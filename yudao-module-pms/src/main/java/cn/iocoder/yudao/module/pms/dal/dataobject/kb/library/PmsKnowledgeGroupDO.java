package cn.iocoder.yudao.module.pms.dal.dataobject.kb.library;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.enums.kb.library.PmsKnowledgeGroupTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 个人知识库分组 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_group")
@KeySequence("pms_knowledge_group_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeGroupDO extends BaseDO {

    /**
     * 知识库分组编号
     */
    @TableId
    private Long id;
    /**
     * 后台用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 分组类型
     *
     * 枚举 {@link PmsKnowledgeGroupTypeEnum}
     */
    private Integer type;

}
