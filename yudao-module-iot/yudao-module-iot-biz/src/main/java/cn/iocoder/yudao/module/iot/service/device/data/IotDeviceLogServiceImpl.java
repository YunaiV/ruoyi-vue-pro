package cn.iocoder.yudao.module.iot.service.device.data;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogMapper;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * IoT 设备日志数据 Service 实现类
 *
 * @author alwayssuper
 */
@Service
@Slf4j
@Validated
public class IotDeviceLogServiceImpl implements IotDeviceLogService {

    @Resource
    private IotDeviceLogMapper deviceLogMapper;

    @Override
    public void defineDeviceLog() {
        if (StrUtil.isNotEmpty(deviceLogMapper.showDeviceLogSTable())) {
            log.info("[defineDeviceLog][设备日志超级表已存在，创建跳过]");
            return;
        }

        log.info("[defineDeviceLog][设备日志超级表不存在，创建开始...]");
        deviceLogMapper.createDeviceLogSTable();
        log.info("[defineDeviceLog][设备日志超级表不存在，创建成功]");
    }

    @Override
    public void createDeviceLog(IotDeviceMessage message) {
        IotDeviceLogDO log = BeanUtils.toBean(message, IotDeviceLogDO.class)
                .setId(IdUtil.fastSimpleUUID())
                .setContent(JsonUtils.toJsonString(message.getData()));
        deviceLogMapper.insert(log);
    }

    @Override
    public PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO) {
        // TODO @芋艿：增加一个表不存在的 try catch
        IPage<IotDeviceLogDO> page = deviceLogMapper.selectPage(
                new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

}
