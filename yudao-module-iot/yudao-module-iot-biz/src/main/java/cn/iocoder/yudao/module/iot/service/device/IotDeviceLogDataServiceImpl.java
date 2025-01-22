package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataSimulatorSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
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
    public void defineDeviceLog() {
        // TODO @super：改成不存在才创建。
//        try {
//            // 创建超级表（使用 IF NOT EXISTS 语句避免重复创建错误）
//            iotDeviceLogDataMapper.createDeviceLogSTable();
//        } catch (Exception e) {
//            if (e.getMessage().contains("already exists")) {
//                log.info("[TDengine] 设备日志超级表已存在，跳过创建");
//                return;
//            }
//            throw e;
//        }
        if(iotDeviceLogDataMapper.checkDeviceLogTableExists()==null){
            log.info("[TDengine] 设备日志超级表不存在，开始创建 {}",iotDeviceLogDataMapper.checkDeviceLogTableExists());
            iotDeviceLogDataMapper.createDeviceLogSTable();
        }else{
            log.info("[TDengine] 设备日志超级表已存在，跳过创建");
        }
    }

    @Override
    public void createDeviceLog(IotDeviceDataSimulatorSaveReqVO simulatorReqVO) {
        // 1. 转换请求对象为 DO
        IotDeviceLogDO iotDeviceLogDO = BeanUtils.toBean(simulatorReqVO, IotDeviceLogDO.class);

        // 2. 处理时间字段
        // TODO @super：一次性的字段，不用单独给个变量
//        long currentTime = System.currentTimeMillis();
        // 2.1 设置时序时间为当前时间
//        iotDeviceLogDO.setTs(currentTime); // TODO @super：TS在SQL中直接NOW   咱们的TS数据获取是走哪一种；走 now()

        // 3. 插入数据
        // TODO @super：不要直接调用对方的 IotDeviceLogDataMapper，通过 service 哈！
        // 讨论：艿菇  这就是iotDeviceLogDataService的Impl
        iotDeviceLogDataMapper.insert(iotDeviceLogDO);
    }

    // TODO @super：在 iotDeviceLogDataService 写
    // 讨论：艿菇  这就是iotDeviceLogDataService的Impl
    @Override
    public PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO) {
        // 查询数据
        List<IotDeviceLogDO> list = iotDeviceLogDataMapper.selectPage(pageReqVO);
        Long total = iotDeviceLogDataMapper.selectCount(pageReqVO);
        // 构造分页结果
        return new PageResult<>(list, total);
    }

    @Override
    public void saveDeviceLog(ThingModelMessage msg) {

    }

}
