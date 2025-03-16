package cn.iocoder.yudao.module.iot.service.device.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.data.IotDeviceLogPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceLogDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IoT 设备日志数据 Service 接口
 *
 * @author alwayssuper
 */
public interface IotDeviceLogService {

    /**
     * 初始化 TDengine 超级表
     *
     * 系统启动时，会自动初始化一次
     */
    void defineDeviceLog();

    /**
     * 插入设备日志
     *
     * @param message 设备数据
     */
    void createDeviceLog(IotDeviceMessage message);

    /**
     * 获得设备日志分页
     *
     * @param pageReqVO 分页查询
     * @return 设备日志分页
     */
    PageResult<IotDeviceLogDO> getDeviceLogPage(IotDeviceLogPageReqVO pageReqVO);

    /**
     * 获得设备日志数量
     *
     * @param createTime 创建时间，如果为空，则统计所有日志数量
     * @return 日志数量
     */
    Long getDeviceLogCount(@Nullable LocalDateTime createTime);

    // TODO @super：deviceKey 是不是用不上哈？
    /**
     * 获得每个小时设备上行消息数量统计
     *
     * @param deviceKey 设备标识
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return key: 时间戳, value: 消息数量
     */
    List<Map<Long, Integer>> getDeviceLogUpCountByHour(@Nullable String deviceKey,
                                                       @Nullable Long startTime,
                                                       @Nullable Long endTime);

    /**
     * 获得每个小时设备下行消息数量统计
     *
     * @param deviceKey 设备标识
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return key: 时间戳, value: 消息数量
     */
    List<Map<Long, Integer>> getDeviceLogDownCountByHour(@Nullable String deviceKey,
                                                         @Nullable Long startTime,
                                                         @Nullable Long endTime);

}
