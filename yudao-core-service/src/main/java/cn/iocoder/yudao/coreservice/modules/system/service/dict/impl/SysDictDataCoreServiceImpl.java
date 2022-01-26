package cn.iocoder.yudao.coreservice.modules.system.service.dict.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.coreservice.modules.system.convert.dict.SysDictDataCoreConvert;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.dict.SysDictDataCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.dict.SysDictDataCoreService;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.dict.core.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.DICT_DATA_NOT_ENABLE;
import static cn.iocoder.yudao.coreservice.modules.system.enums.SysErrorCodeConstants.DICT_DATA_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 字典数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysDictDataCoreServiceImpl implements SysDictDataCoreService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 字典数据缓存，第二个 key 使用 label
     *
     * key1：字典类型 dictType
     * key2：字典标签 label
     */
    private ImmutableTable<String, String, SysDictDataDO> labelDictDataCache;
    /**
     * 字典数据缓存，第二个 key 使用 value
     *
     * key1：字典类型 dictType
     * key2：字典值 value
     */
    private ImmutableTable<String, String, SysDictDataDO> valueDictDataCache;
    /**
     * 缓存字典数据的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    @Resource
    private SysDictDataCoreMapper dictDataCoreMapper;

    @Override
    @PostConstruct
    public synchronized void initLocalCache() {
        // 获取字典数据列表，如果有更新
        List<SysDictDataDO> dataList = this.loadDictDataIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(dataList)) {
            return;
        }

        // 构建缓存
        ImmutableTable.Builder<String, String, SysDictDataDO> labelDictDataBuilder = ImmutableTable.builder();
        ImmutableTable.Builder<String, String, SysDictDataDO> valueDictDataBuilder = ImmutableTable.builder();
        dataList.forEach(dictData -> {
            labelDictDataBuilder.put(dictData.getDictType(), dictData.getLabel(), dictData);
            valueDictDataBuilder.put(dictData.getDictType(), dictData.getValue(), dictData);
        });
        labelDictDataCache = labelDictDataBuilder.build();
        valueDictDataCache = valueDictDataBuilder.build();
        assert dataList.size() > 0; // 断言，避免告警
        maxUpdateTime = dataList.stream().max(Comparator.comparing(BaseDO::getUpdateTime)).get().getUpdateTime();
        log.info("[initLocalCache][缓存字典数据，数量为:{}]", dataList.size());
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 如果字典数据发生变化，从数据库中获取最新的全量字典数据。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前字典数据的最大更新时间
     * @return 字典数据列表
     */
    private List<SysDictDataDO> loadDictDataIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadDictDataIfUpdate][首次加载全量字典数据]");
        } else { // 判断数据库中是否有更新的字典数据
            if (!dictDataCoreMapper.selectExistsByUpdateTimeAfter(maxUpdateTime)) {
                return null;
            }
            log.info("[loadDictDataIfUpdate][增量加载全量字典数据]");
        }
        // 第二步，如果有更新，则从数据库加载所有字典数据
        return dictDataCoreMapper.selectList();
    }

    @Override
    public DictDataRespDTO getDictDataFromCache(String type, String value) {
        return SysDictDataCoreConvert.INSTANCE.convert02(valueDictDataCache.get(type, value));
    }

    @Override
    public DictDataRespDTO parseDictDataFromCache(String type, String label) {
        return SysDictDataCoreConvert.INSTANCE.convert02(labelDictDataCache.get(type, label));
    }

    @Override
    public List<DictDataRespDTO> listDictDatasFromCache(String type) {
        return SysDictDataCoreConvert.INSTANCE.convertList03(labelDictDataCache.row(type).values());
    }

    @Override
    public void validDictDatas(String dictType, Collection<String> values) {
        if (CollUtil.isEmpty(values)) {
            return;
        }
        ImmutableMap<String, SysDictDataDO> dictDataMap = valueDictDataCache.row(dictType);
        // 校验
        values.forEach(value -> {
            SysDictDataDO dictData = dictDataMap.get(value);
            if (dictData == null) {
                throw exception(DICT_DATA_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dictData.getStatus())) {
                throw exception(DICT_DATA_NOT_ENABLE, dictData.getLabel());
            }
        });
    }

}
