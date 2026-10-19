package cn.iocoder.yudao.module.oa.service.meetingroom;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking.*;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 会议室 Service 接口
 *
 * @author 芋道源码
 */
public interface OaMeetingRoomService {

    /**
     * 创建会议室
     *
     * @param createReqVO 会议室信息
     * @return 编号
     */
    Long createMeetingRoom(@Valid OaMeetingRoomSaveReqVO createReqVO);

    /**
     * 更新会议室
     *
     * @param updateReqVO 会议室信息
     */
    void updateMeetingRoom(@Valid OaMeetingRoomSaveReqVO updateReqVO);

    /**
     * 删除会议室
     *
     * @param id 会议室编号
     */
    void deleteMeetingRoom(Long id);

    /**
     * 校验会议室存在
     *
     * @param id 会议室编号
     * @return 会议室
     */
    OaMeetingRoomDO validateMeetingRoomExists(Long id);

    /**
     * 获得会议室分页
     *
     * @param pageReqVO 查询条件
     * @return 分页结果
     */
    PageResult<OaMeetingRoomDO> getMeetingRoomPage(OaMeetingRoomPageReqVO pageReqVO);

    /**
     * 获得会议室列表
     *
     * @param ids 会议室编号
     * @return 会议室列表
     */
    List<OaMeetingRoomDO> getMeetingRoomList(Collection<Long> ids);

    /**
     * 获得会议室 Map
     *
     * @param ids 会议室编号
     * @return 会议室 Map
     */
    default Map<Long, OaMeetingRoomDO> getMeetingRoomMap(Collection<Long> ids) {
        List<OaMeetingRoomDO> rooms = getMeetingRoomList(ids);
        return convertMap(rooms, OaMeetingRoomDO::getId);
    }

    /**
     * 按名称查询会议室
     *
     * @param name 名称关键字
     * @return 会议室列表
     */
    List<OaMeetingRoomDO> getMeetingRoomListByName(String name);

    /**
     * 获得本人可预定会议室分页
     *
     * @param pageReqVO 查询条件
     * @param userId 用户编号
     * @return 会议室分页
     */
    PageResult<OaMeetingRoomDO> getMeetingRoomPageByBookingUserId(OaMeetingRoomPageReqVO pageReqVO, Long userId);

}
