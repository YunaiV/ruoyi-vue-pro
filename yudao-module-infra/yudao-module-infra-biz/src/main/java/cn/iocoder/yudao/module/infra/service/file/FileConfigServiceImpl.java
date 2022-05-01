package cn.iocoder.yudao.module.infra.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.file.core.client.FileClient;
import cn.iocoder.yudao.framework.file.core.client.FileClientConfig;
import cn.iocoder.yudao.framework.file.core.client.FileClientFactory;
import cn.iocoder.yudao.framework.file.core.enums.FileStorageEnum;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.convert.file.FileConfigConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileConfigMapper;
import cn.iocoder.yudao.module.infra.mq.producer.file.FileConfigProducer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Validator;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_DELETE_FAIL_MASTER;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.FILE_CONFIG_NOT_EXISTS;

/**
 * 文件配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class FileConfigServiceImpl implements FileConfigService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    @Getter
    private volatile Date maxUpdateTime;

    @Resource
    private FileClientFactory fileClientFactory;
    /**
     * Master FileClient 对象，有且仅有一个，即 {@link FileConfigDO#getMaster()} 对应的
     */
    @Getter
    private FileClient masterFileClient;

    @Resource
    private FileConfigMapper fileConfigMapper;

    @Resource
    private FileConfigProducer fileConfigProducer;

    @Resource
    private Validator validator;

    @Resource
    @Lazy // 注入自己，所以延迟加载
    private FileConfigService self;

    @Override
    @PostConstruct
    public void initFileClients() {
        // 获取文件配置，如果有更新
        List<FileConfigDO> configs = loadFileConfigIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(configs)) {
            return;
        }

        // 创建或更新支付 Client
        configs.forEach(config -> {
            fileClientFactory.createOrUpdateFileClient(config.getId(), config.getStorage(), config.getConfig());
            // 如果是 master，进行设置
            if (Boolean.TRUE.equals(config.getMaster())) {
                masterFileClient = fileClientFactory.getFileClient(config.getId());
            }
        });

        // 写入缓存
        maxUpdateTime = CollectionUtils.getMaxValue(configs, FileConfigDO::getUpdateTime);
        log.info("[initFileClients][初始化 FileConfig 数量为 {}]", configs.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        self.initFileClients();
    }

    /**
     * 如果文件配置发生变化，从数据库中获取最新的全量文件配置。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前文件配置的最大更新时间
     * @return 文件配置列表
     */
    private List<FileConfigDO> loadFileConfigIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadFileConfigIfUpdate][首次加载全量文件配置]");
        } else { // 判断数据库中是否有更新的文件配置
            if (fileConfigMapper.selectCountByUpdateTimeGt(maxUpdateTime) == 0) {
                return null;
            }
            log.info("[loadFileConfigIfUpdate][增量加载全量文件配置]");
        }
        // 第二步，如果有更新，则从数据库加载所有文件配置
        return fileConfigMapper.selectList();
    }

    @Override
    public Long createFileConfig(FileConfigCreateReqVO createReqVO) {
        // 插入
        FileConfigDO fileConfig = FileConfigConvert.INSTANCE.convert(createReqVO)
                .setConfig(parseClientConfig(createReqVO.getStorage(), createReqVO.getConfig()))
                .setMaster(false); // 默认非 master
        fileConfigMapper.insert(fileConfig);
        // 发送刷新配置的消息
        fileConfigProducer.sendFileConfigRefreshMessage();
        // 返回
        return fileConfig.getId();
    }

    @Override
    public void updateFileConfig(FileConfigUpdateReqVO updateReqVO) {
        // 校验存在
        FileConfigDO config = this.validateFileConfigExists(updateReqVO.getId());
        // 更新
        FileConfigDO updateObj = FileConfigConvert.INSTANCE.convert(updateReqVO)
                .setConfig(parseClientConfig(config.getStorage(), updateReqVO.getConfig()));
        fileConfigMapper.updateById(updateObj);
        // 发送刷新配置的消息
        fileConfigProducer.sendFileConfigRefreshMessage();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileConfigMaster(Long id) {
        // 校验存在
        this.validateFileConfigExists(id);
        // 更新其它为非 master
        fileConfigMapper.updateBatch(new FileConfigDO().setMaster(false));
        // 更新
        fileConfigMapper.updateById(new FileConfigDO().setId(id).setMaster(true));
        // 发送刷新配置的消息
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                fileConfigProducer.sendFileConfigRefreshMessage();
            }

        });
    }

    private FileClientConfig parseClientConfig(Integer storage, Map<String, Object> config) {
        // 获取配置类
        Class<? extends FileClientConfig> configClass = FileStorageEnum.getByStorage(storage)
                .getConfigClass();
        FileClientConfig clientConfig = JsonUtils.parseObject2(JsonUtils.toJsonString(config), configClass);
        // 参数校验
        ValidationUtils.validate(validator, clientConfig);
        // 设置参数
        return clientConfig;
    }

    @Override
    public void deleteFileConfig(Long id) {
        // 校验存在
        FileConfigDO config = this.validateFileConfigExists(id);
        if (Boolean.TRUE.equals(config.getMaster())) {
             throw exception(FILE_CONFIG_DELETE_FAIL_MASTER);
        }
        // 删除
        fileConfigMapper.deleteById(id);
        // 发送刷新配置的消息
        fileConfigProducer.sendFileConfigRefreshMessage();
    }

    private FileConfigDO validateFileConfigExists(Long id) {
        FileConfigDO config = fileConfigMapper.selectById(id);
        if (config == null) {
            throw exception(FILE_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    @Override
    public FileConfigDO getFileConfig(Long id) {
        return fileConfigMapper.selectById(id);
    }

    @Override
    public List<FileConfigDO> getFileConfigList(Collection<Long> ids) {
        return fileConfigMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<FileConfigDO> getFileConfigPage(FileConfigPageReqVO pageReqVO) {
        return fileConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public String testFileConfig(Long id) throws Exception {
        // 校验存在
        this.validateFileConfigExists(id);
        // 上传文件
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        return fileClientFactory.getFileClient(id).upload(content, IdUtil.fastSimpleUUID() + ".jpg");
    }

    @Override
    public FileClient getFileClient(Long id) {
        return fileClientFactory.getFileClient(id);
    }

}
