package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.tdengine.IotDeviceLogDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * IoT 设备日志数据 Service 实现了
 *
 * @author alwayssuper
 */
@Service
@Slf4j
@Validated
public class IotDeviceLogDataServiceImpl implements IotDeviceLogDataService{

    @Resource
    private IotDeviceLogDataMapper iotDeviceLogDataMapper;

    // TODO @super：方法名。defineDeviceLog。。未来，有可能别人使用别的记录日志，例如说 es 之类的。
    @Override
    public void initTDengineSTable() {
        // TODO @super：改成不存在才创建。
        iotDeviceLogDataMapper.createDeviceLogSTable();
    }

    @Override
    public void createDeviceLog(IotDeviceDataSimulatorSaveReqVO simulatorReqVO) {
        // 1. 转换请求对象为 DO
        IotDeviceLogDO iotDeviceLogDO = BeanUtils.toBean(simulatorReqVO, IotDeviceLogDO.class);

        // 2. 处理时间字段
        // TODO @super：一次性的字段，不用单独给个变量
        long currentTime = System.currentTimeMillis();
        // 2.1 设置时序时间为当前时间
        iotDeviceLogDO.setTs(currentTime); // TODO @super：TS在SQL中直接NOW   咱们的TS数据获取是走哪一种；走 now()

        // 3. 插入数据
        // TODO @super：不要直接调用对方的 IotDeviceLogDataMapper，通过 service 哈！
        iotDeviceLogDataMapper.insert(iotDeviceLogDO);
    }

    // TODO @super：在 iotDeviceLogDataService 写
    @Override
    public PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO) {
        // 查询数据
        List<IotDeviceLogDO> list = iotDeviceLogDataMapper.selectPage(pageReqVO);
        Long total = iotDeviceLogDataMapper.selectCount(pageReqVO);
        // 构造分页结果
        return new PageResult<>(list, total);
    }

}
