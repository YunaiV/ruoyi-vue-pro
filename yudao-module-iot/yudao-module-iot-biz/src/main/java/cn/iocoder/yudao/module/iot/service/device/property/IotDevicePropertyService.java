package cn.iocoder.yudao.module.iot.service.device.property;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
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
     * @param listReqVO 列表请求
     * @return 设备属性历史数据
     */
    List<IotDevicePropertyRespVO> getHistoryDevicePropertyList(@Valid IotDevicePropertyHistoryListReqVO listReqVO);

    // ========== 设备时间相关操作 ==========

    /**
     * 获得最后上报时间小于指定时间的设备编号集合
     *
     * @param maxReportTime 最大上报时间
     * @return 设备编号集合
     */
    Set<Long> getDeviceIdListByReportTime(LocalDateTime maxReportTime);

    /**
     * 更新设备上报时间
     *
     * @param id 设备编号
     * @param reportTime 上报时间
     */
    void updateDeviceReportTimeAsync(Long id, LocalDateTime reportTime);

    /**
     * 更新设备关联的网关服务 serverId
     *
     * @param id 设备编号
     * @param serverId 网关 serverId
     */
    void updateDeviceServerIdAsync(Long id, String serverId);

    /**
     * 获得设备关联的网关服务 serverId
     *
     * @param id 设备编号
     * @return 网关 serverId
     */
    String getDeviceServerId(Long id);

}