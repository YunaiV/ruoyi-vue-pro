package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDevicePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDeviceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.IotDeviceStatusUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import jakarta.validation.Valid;

/**
 * IoT 设备数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceDataService {


    /**
     * 保存设备数据
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @param message     消息
     */
    void saveDeviceData(String productKey, String deviceName, String message);
}