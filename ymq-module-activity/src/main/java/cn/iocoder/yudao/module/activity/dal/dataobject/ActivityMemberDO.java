package cn.iocoder.yudao.module.activity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动参与者 DO。docs/5-数据库设计.md §3.2。
 *
 * 一行 = 一次报名。user_id 可为空（占位人，召集人代报）；status 是状态机驱动。
 */
@TableName("t_activity_member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMemberDO implements Serializable {

    @TableId
    private Long id;

    private Long activityId;
    /** 注册用户 ID。占位人为 null */
    private Long userId;
    /** 占位人名（user_id 为 null 时使用） */
    private String placeholderName;
    /** 0未知 1男 2女 */
    private Integer gender;
    /** 报名时段位快照（用于排对阵） */
    private String levelAtJoin;
    /** INVITE/MANUAL/DISCOVERY */
    private String joinSource;

    private LocalDateTime joinedAt;
    /** JOINED/WAITING/QUIT/REMOVED/NO_SHOW */
    private String status;
    private String note;

}
