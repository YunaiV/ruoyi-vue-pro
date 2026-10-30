package cn.iocoder.yudao.module.iot.service.device.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.message.IotDeviceMessagePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 设备消息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageService {

    /**
     * 发送设备消息
     *
     * @param message 消息（“codec（编解码）字段” 部分字段）
     * @param device 设备
     * @return 设备消息
     */
    IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device);

    /**
     * 发送设备消息（指定 serverId，用于上行回复等场景）
     *
     * @param message  消息
     * @param device   设备
     * @param serverId 网关 serverId
     * @return 设备消息
     */
    IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId);

    /**
     * 发送设备消息（指定事件时间）
     *
     * 用于 biz 派生的消息沿用源事件的时间，例如说：强制上线、网关下线联动子设备、离线检测。仅供内部调用，不对外暴露
     *
     * @param message   消息
     * @param device    设备
     * @param serverId  网关 serverId
     * @param eventTime 事件时间，作为消息的 reportTime；为空时使用当前时间
     * @return 设备消息
     */
    IotDeviceMessage sendDeviceMessage(IotDeviceMessage message, IotDeviceDO device, String serverId,
                                       @Nullable LocalDateTime eventTime);

    /**
     * 发送设备消息
     *
     * @param message 消息（“codec（编解码）字段” 部分字段）
     * @return 设备消息
     */
    IotDeviceMessage sendDeviceMessage(IotDeviceMessage message);

    /**
     * 处理设备上行的消息，包括如下步骤：
     *
     * 1. 处理消息
     * 2. 记录消息
     * 3. 回复消息
     *
     * @param message 消息
     * @param device 设备
     */
    void handleUpstreamDeviceMessage(IotDeviceMessage message, IotDeviceDO device);

    /**
     * 获得设备消息分页
     *
     * @param pageReqVO 分页查询
     * @return 设备消息分页
     */
    PageResult<IotDeviceMessageDO> getDeviceMessagePage(IotDeviceMessagePageReqVO pageReqVO);

    /**
     * 获得指定 requestId 的设备消息列表
     *
     * @param deviceId 设备编号
     * @param requestIds requestId 列表
     * @param reply 是否回复
     * @return 设备消息列表
     */
    List<IotDeviceMessageDO> getDeviceMessageListByRequestIdsAndReply(
            @NotNull(message = "设备编号不能为空") Long deviceId,
            List<String> requestIds,
            Boolean reply);

    /**
     * 获得设备消息数量
     *
     * @param createTime 创建时间，如果为空，则统计所有消息数量
     * @return 消息数量
     */
    Long getDeviceMessageCount(@Nullable LocalDateTime createTime);

    /**
     * 获取设备消息的数据统计
     *
     * @param reqVO 统计请求
     * @return 设备消息的数据统计
     */
    List<IotStatisticsDeviceMessageSummaryByDateRespVO> getDeviceMessageSummaryByDate(
            IotStatisticsDeviceMessageReqVO reqVO);

}
