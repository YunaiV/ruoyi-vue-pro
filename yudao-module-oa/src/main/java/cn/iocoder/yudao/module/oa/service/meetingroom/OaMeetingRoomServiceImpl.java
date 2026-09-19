package cn.iocoder.yudao.module.oa.service.meetingroom;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.OaMeetingRoomDO;
import cn.iocoder.yudao.module.oa.dal.mysql.meetingroom.OaMeetingRoomMapper;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.meetingroom.*;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 会议室 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaMeetingRoomServiceImpl implements OaMeetingRoomService {

    @Resource
    private OaMeetingRoomMapper meetingRoomMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DictDataApi dictDataApi;

    @Override
    public Long createMeetingRoom(OaMeetingRoomSaveReqVO createReqVO) {
        // 1. 校验负责人、设备和可预定成员
        validateMeetingRoom(createReqVO);

        // 2. 保存会议室
        OaMeetingRoomDO room = BeanUtils.toBean(createReqVO, OaMeetingRoomDO.class);
        meetingRoomMapper.insert(room);
        return room.getId();
    }

    @Override
    public void updateMeetingRoom(OaMeetingRoomSaveReqVO updateReqVO) {
        // 1.1 校验会议室存在
        validateMeetingRoomExists(updateReqVO.getId());
        // 1.2 校验配置
        validateMeetingRoom(updateReqVO);

        // 2. 更新当前配置
        meetingRoomMapper.updateById(BeanUtils.toBean(updateReqVO, OaMeetingRoomDO.class));
    }

    @Override
    public void deleteMeetingRoom(Long id) {
        // 1.1 校验会议室存在
        validateMeetingRoomExists(id);
        // 1.2 校验没有仍有效的预约
        if (meetingRoomBookingService.hasActiveMeetingRoomBooking(id)) {
            throw exception(MEETING_ROOM_HAS_BOOKING);
        }

        // 2. 删除会议室
        meetingRoomMapper.deleteById(id);
    }

    @Override
    public OaMeetingRoomDO validateMeetingRoomExists(Long id) {
        OaMeetingRoomDO room = meetingRoomMapper.selectById(id);
        if (room == null) {
            throw exception(MEETING_ROOM_NOT_EXISTS);
        }
        return room;
    }

    @Override
    public PageResult<OaMeetingRoomDO> getMeetingRoomPage(OaMeetingRoomPageReqVO pageReqVO) {
        return getMeetingRoomPageByBookingUserId(pageReqVO, null);
    }

    @Override
    public PageResult<OaMeetingRoomDO> getMeetingRoomPageByBookingUserId(OaMeetingRoomPageReqVO pageReqVO, Long userId) {
        // 1. 按负责人姓名查询匹配用户，空结果不继续查询
        List<Long> managerIds = null;
        if (StrUtil.isNotBlank(pageReqVO.getManagerName())) {
            managerIds = convertList(adminUserApi.getUserListByNickname(pageReqVO.getManagerName()), AdminUserRespDTO::getId);
            if (CollUtil.isEmpty(managerIds)) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }
        // 2. 查询会议室分页
        return meetingRoomMapper.selectPage(pageReqVO, managerIds, userId);
    }

    @Override
    public List<OaMeetingRoomDO> getMeetingRoomList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return meetingRoomMapper.selectByIds(ids);
    }

    @Override
    public List<OaMeetingRoomDO> getMeetingRoomListByName(String name) {
        return meetingRoomMapper.selectListByName(name);
    }

    /**
     * 校验会议室配置
     *
     * @param reqVO 会议室配置
     */
    private void validateMeetingRoom(OaMeetingRoomSaveReqVO reqVO) {
        // 1. 校验负责人有效
        adminUserApi.validateUser(reqVO.getManagerUserId());
        // 2. 校验设备字典值
        if (CollUtil.isNotEmpty(reqVO.getEquipments())) {
            dictDataApi.validateDictDataList(DictTypeConstants.MEETING_ROOM_EQUIPMENT,
                    convertList(reqVO.getEquipments(), String::valueOf));
        }
        // 3. 校验指定成员范围
        if (ObjUtil.equal(reqVO.getBookingScope(), OaMeetingRoomBookingScopeEnum.SPECIFIED.getScope())) {
            if (CollUtil.isEmpty(reqVO.getBookingUserIds())) {
                throw exception(MEETING_ROOM_SCOPE_INVALID);
            }
            adminUserApi.validateUserList(reqVO.getBookingUserIds());
        }
    }
}
