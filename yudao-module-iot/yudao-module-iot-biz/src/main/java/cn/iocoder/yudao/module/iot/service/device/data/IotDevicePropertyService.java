package cn.iocoder.yudao.module.iot.service.device.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyHistoryPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDevicePropertyDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

import javax.validation.Valid;
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
     * @param message 设备消息
     */
    void saveDeviceProperty(IotDeviceMessage message);

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
     * @return 设备标识列表
     */
    Set<String> getDeviceKeysByReportTime(LocalDateTime maxReportTime);

    /**
     * 异步更新设备上报时间
     *
     * @param deviceKey  设备标识
     * @param reportTime 上报时间
     */
    void updateDeviceReportTimeAsync(String deviceKey, LocalDateTime reportTime);

}