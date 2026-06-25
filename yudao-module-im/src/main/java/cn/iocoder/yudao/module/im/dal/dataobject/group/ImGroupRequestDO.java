package cn.iocoder.yudao.module.im.dal.dataobject.group;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.enums.group.ImGroupAddSourceEnum;
import cn.iocoder.yudao.module.im.enums.group.ImGroupRequestHandleResultEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * IM 加群申请记录 DO
 * <p>
 * 配合「申请 - 审批」流程：
 * <ul>
 *     <li>用户主动申请：调 apply 接口落库（inviterUserId=null，handleResult=UNHANDLED），群主 / 管理员审批</li>
 *     <li>普通成员邀请：群 joinType=APPLY_AND_NORMAL_INVITE 时落库（inviterUserId=操作人），群主 / 管理员审批</li>
 *     <li>处理：群主 / 管理员调 agree / refuse 推进状态机；同意时把 addSource / inviterUserId 同步写入 {@link ImGroupMemberDO}</li>
 * </ul>
 *
 * @author 芋道源码
 */
@TableName("im_group_request")
@KeySequence("im_group_request_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增；MySQL 等可不写
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImGroupRequestDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 群编号
     * <p>
     * 关联 {@link ImGroupDO#getId()}
     */
    private Long groupId;
    /**
     * 申请人 / 被邀请人用户编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 邀请人用户编号
     * <p>
     * NULL 表示用户主动申请进群；非 NULL 表示由群成员邀请、待群主 / 管理员审批
     */
    private Long inviterUserId;

    // ========== 申请发起阶段 ==========
    /**
     * 申请理由
     */
    private String applyContent;
    /**
     * 加入来源
     * <p>
     * 枚举 {@link ImGroupAddSourceEnum}；同意时同步写入 {@link ImGroupMemberDO#getAddSource()}
     */
    private Integer addSource;

    // ========== 申请处理阶段 ==========
    /**
     * 处理结果
     * <p>
     * 枚举 {@link ImGroupRequestHandleResultEnum}
     */
    private Integer handleResult;
    /**
     * 处理人用户编号（群主或管理员）
     */
    private Long handleUserId;
    /**
     * 处理理由（拒绝时可选填）
     */
    private String handleContent;
    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

}
