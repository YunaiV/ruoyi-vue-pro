package cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomBookingScopeEnum;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomStatusEnum;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomTypeEnum;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * OA 会议室 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_meeting_room", autoResultMap = true)
@KeySequence("oa_meeting_room_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaMeetingRoomDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 会议室名称
     */
    private String name;
    /**
     * 会议室位置
     */
    private String location;
    /**
     * 会议室类型
     *
     * 枚举 {@link OaMeetingRoomTypeEnum}
     */
    private Integer type;
    /**
     * 负责人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     * 联系方式使用 {@link AdminUserRespDTO#getMobile()}
     */
    private Long managerUserId;
    /**
     * 可用状态
     *
     * 枚举 {@link OaMeetingRoomStatusEnum}
     */
    private Integer status;
    /**
     * 会议室图片 URL
     */
    private String picUrl;
    /**
     * 坐席数
     */
    private Integer seatCount;
    /**
     * 设备列表
     *
     * 字典 {@link DictTypeConstants#MEETING_ROOM_EQUIPMENT}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> equipments;
    /**
     * 是否允许预定
     */
    private Boolean allowBooking;
    /**
     * 预定是否需要审批
     */
    private Boolean needApproval;
    /**
     * 可预定范围
     *
     * 枚举 {@link OaMeetingRoomBookingScopeEnum}
     */
    private Integer bookingScope;
    /**
     * 可预定用户编号列表
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> bookingUserIds;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

}
