package cn.iocoder.yudao.module.pms.dal.dataobject.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 知识内容协作者 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_content_permission_member")
@KeySequence("pms_knowledge_content_permission_member_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeContentPermissionMemberDO extends BaseDO {

    /**
     * 协作者编号
     */
    @TableId
    private Long id;
    /**
     * 内容协作权限编号
     *
     * 关联 {@link PmsKnowledgeContentPermissionDO#getId()}
     */
    private Long permissionId;
    /**
     * 后台用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 部门编号
     *
     * 关联 {@link DeptDO#getId()}
     */
    private Long deptId;
    /**
     * 协作等级
     *
     * 枚举 {@link PmsKnowledgeContentLevelEnum}
     */
    private Integer level;
}
