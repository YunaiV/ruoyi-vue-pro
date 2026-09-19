package cn.iocoder.yudao.module.oa.service.meetingroom;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room.*;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 会议室预定 Service 接口
 *
 * @author 芋道源码
 */
public interface OaMeetingRoomBookingService {

    /**
     * 创建预定草稿
     *
     * @param createReqVO 预定信息
     * @param userId 当前用户编号
     * @return 预定编号
     */
    Long createMeetingRoomBooking(@Valid OaMeetingRoomBookingSaveReqVO createReqVO, Long userId);

    /**
     * 更新预定草稿
     *
     * @param updateReqVO 预定信息
     * @param userId 当前用户编号
     */
    void updateMeetingRoomBooking(@Valid OaMeetingRoomBookingSaveReqVO updateReqVO, Long userId);

    /**
     * 删除预定草稿
     *
     * @param id 预定编号
     * @param userId 当前用户编号
     */
    void deleteMeetingRoomBooking(Long id, Long userId);

    /**
     * 提交预定，需要审批时启动流程
     *
     * @param id 预定编号
     * @param userId 当前用户编号
     * @return 流程实例编号，免审批时为空
     */
    String submitMeetingRoomBooking(Long id, Long userId);

    /**
     * 取消审批中或已通过的待使用预约
     *
     * @param id 预定编号
     * @param userId 当前用户编号
     */
    void cancelMeetingRoomBooking(Long id, Long userId);

    /**
     * 开始使用会议室
     *
     * @param id 预定编号
     * @param userId 当前用户编号
     */
    void startMeetingRoomBooking(Long id, Long userId);

    /**
     * 完成会议室使用
     *
     * @param id 预定编号
     * @param userId 当前用户编号
     */
    void finishMeetingRoomBooking(Long id, Long userId);

    /**
     * 回写审批状态
     *
     * @param id 预定编号
     * @param status 审批状态
     */
    void updateMeetingRoomBookingStatus(Long id, Integer status);

    /**
     * 获得预定详情
     *
     * @param id 预定编号
     * @return 预定
     */
    OaMeetingRoomBookingDO getMeetingRoomBooking(Long id);

    /**
     * 获得本人预定分页
     *
     * @param userId 当前用户编号
     * @param pageReqVO 查询条件
     * @return 分页结果
     */
    PageResult<OaMeetingRoomBookingDO> getMeetingRoomBookingPage(Long userId, OaMeetingRoomBookingPageReqVO pageReqVO);

    /**
     * 获得会议室占用时段
     *
     * @param reqVO 房间和查询区间
     * @return 有效预约列表
     */
    List<OaMeetingRoomBookingDO> getMeetingRoomBookingListBySchedule(OaMeetingRoomBookingScheduleReqVO reqVO);

    /**
     * 取消已过结束时间、审批通过但尚未开始的会议预定
     *
     * @return 取消的会议数量
     */
    int cancelExpiredMeetingRoomBookings();

    /**
     * 发送已通过且待使用会议的开始提醒
     *
     * @return 成功提醒的会议数量
     */
    int sendMeetingRoomBookingReminders();

    /**
     * 判断会议室是否存在尚未结束的有效预约
     *
     * @param roomId 会议室编号
     * @return 是否存在
     */
    boolean hasActiveMeetingRoomBooking(Long roomId);

}
