package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDataDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * IoT 设备【属性】数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDevicePropertyDataService {

    /**
     * 定义设备属性数据的结构
     *
     * @param productId 产品编号
     */
    void defineDevicePropertyData(Long productId);

    /**
     * 保存设备数据
     *
     * @param productKey 产品 key
     * @param deviceName 设备名称
     * @param message    消息
     *                   <p>参见 <a href="https://help.aliyun.com/zh/iot/user-guide/device-properties-events-and-services?spm=a2c4g.11186623.0.0.3a3335aeUdzkz2#concept-mvc-4tw-y2b">JSON 格式</a>
     */
    void saveDeviceData(String productKey, String deviceName, String message);

    /**
     * 获得设备属性最新数据
     *
     * @param deviceId 设备编号
     * @return 设备属性最新数据
     */
    List<IotDeviceDataDO> getLatestDeviceProperties(@Valid IotDeviceDataPageReqVO deviceId);

    /**
     * 获得设备属性历史数据
     *
     * @param deviceDataReqVO 设备属性历史数据 Request VO
     * @return 设备属性历史数据
     */
    PageResult<Map<String, Object>> getHistoryDeviceProperties(@Valid IotDeviceDataPageReqVO deviceDataReqVO);

}