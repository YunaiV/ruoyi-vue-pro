package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;
import cn.iocoder.yudao.module.iot.dal.storage.IotDeviceMessageDAO;
import cn.iocoder.yudao.module.iot.framework.storage.config.IotStorageProperties;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于 TDengine 的 {@link IotDeviceMessageDAO} 实现类
 *
 * 一张超级表 device_message，每个设备一张子表 device_message_{deviceId}
 *
 * @author 芋道源码
 */
@Repository
@ConditionalOnProperty(prefix = IotStorageProperties.PREFIX, name = "type", havingValue = "tdengine", matchIfMissing = true)
@Slf4j
public class IotDeviceMessageTDengineDAO implements IotDeviceMessageDAO {

    @Resource
    private IotDeviceMessageMapper deviceMessageMapper;

    @Override
    public void initialize() {
        if (StrUtil.isNotEmpty(deviceMessageMapper.showSTable())) {
            log.info("[initialize][设备消息超级表已存在，创建跳过]");
            return;
        }
        log.info("[initialize][设备消息超级表不存在，创建开始...]");
        deviceMessageMapper.createSTable();
        log.info("[initialize][设备消息超级表不存在，创建成功]");
    }

    @Override
    public void insert(IotDeviceMessageDO message) {
        deviceMessageMapper.insert(message);
    }

    @Override
    public PageResult<IotDeviceMessageDO> selectPage(IotDeviceMessagePageReqVO reqVO) {
        try {
            IPage<IotDeviceMessageDO> page = deviceMessageMapper.selectPage(
                    new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), reqVO);
            return new PageResult<>(fillDeviceId(page.getRecords(), reqVO.getDeviceId()), page.getTotal());
        } catch (Exception exception) {
            if (StrUtil.contains(exception.getMessage(), "Table does not exist")) {
                return PageResult.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<IotDeviceMessageDO> selectListByRequestIdsAndReply(Long deviceId, Collection<String> requestIds, Boolean reply) {
        // reply 为空时，reply = NULL 匹配不到任何消息，无需查询
        if (reply == null || requestIds == null) {
            return Collections.emptyList();
        }
        // 与 IoTDB 保持一致：忽略 null、去重，避免 IN 列表中绑定 NULL
        List<String> validRequestIds = requestIds.stream().filter(Objects::nonNull).distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validRequestIds)) {
            return Collections.emptyList();
        }
        return fillDeviceId(deviceMessageMapper.selectListByRequestIdsAndReply(deviceId, validRequestIds, reply), deviceId);
    }

    /**
     * 补全消息的设备编号
     *
     * 原因：查询的是设备子表 device_message_{deviceId}，SELECT 未包含 device_id 标签，读出的 deviceId 为空
     *
     * @param messages 消息列表
     * @param deviceId 设备编号
     * @return 消息列表
     */
    private static List<IotDeviceMessageDO> fillDeviceId(List<IotDeviceMessageDO> messages, Long deviceId) {
        if (messages != null) {
            messages.forEach(message -> message.setDeviceId(deviceId));
        }
        return messages;
    }

    @Override
    public Long selectCountByCreateTime(Long createTime) {
        return deviceMessageMapper.selectCountByCreateTime(createTime);
    }

    @Override
    public List<Map<String, Object>> selectDeviceMessageCountGroupByDate(Long startTime, Long endTime) {
        List<Map<String, Object>> counts = deviceMessageMapper.selectDeviceMessageCountGroupByDate(startTime, endTime);
        return convertList(counts, count -> IotDeviceMessageDAO.buildMessageCount(
                count.get(COUNT_TIME), count.get(COUNT_UPSTREAM), count.get(COUNT_DOWNSTREAM)));
    }

}
