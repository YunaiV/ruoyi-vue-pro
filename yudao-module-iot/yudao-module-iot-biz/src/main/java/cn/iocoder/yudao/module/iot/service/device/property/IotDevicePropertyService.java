package cn.iocoder.yudao.module.iot.service.device.property;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyHistoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * IoT 设备【属性】数据 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDevicePropertyService {

    // ========== 设备属性相关操作 ==========

    /**
     * 定义设备属性数据的结构
     *
     * @param productId 产品编号
     */
    void defineDevicePropertyData(Long productId);

    /**
     * 保存设备数据
     *
     * @param device 设备
     * @param message 设备消息
     */
    void saveDeviceProperty(IotDeviceDO device, IotDeviceMessage message);

    /**
     * 获得设备属性最新数据
     *
     * @param deviceId 设备编号
     * @return 设备属性最新数据
     */
    Map<String, IotDevicePropertyDO> getLatestDeviceProperties(Long deviceId);

    /**
     * 获得设备属性历史数据
     *
     * @param pageReqVO 分页请求
     * @return 设备属性历史数据
     */
    PageResult<IotDevicePropertyRespVO> getHistoryDevicePropertyPage(@Valid IotDevicePropertyHistoryPageReqVO pageReqVO);

    // ========== 设备时间相关操作 ==========

    /**
     * 获得最后上报时间小于指定时间的设备标识
     *
     * @param maxReportTime 最大上报时间
     * @return [productKey, deviceName] 列表
     */
    Set<String[]> getProductKeyDeviceNameListByReportTime(LocalDateTime maxReportTime);

    /**
     * 更新设备上报时间
     *
     * @param productKey  产品标识
     * @param deviceName  设备名称
     * @param reportTime 上报时间
     */
    void updateDeviceReportTime(String productKey, String deviceName, LocalDateTime reportTime);

    /**
     * 更新设备关联的网关 serverId
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @param serverId 网关 serverId
     */
    void updateDeviceServerId(String productKey, String deviceName, String serverId);

    /**
     * 获得设备关联的网关 serverId
     *
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @return 网关 serverId
     */
    String getDeviceServerId(String productKey, String deviceName);

}