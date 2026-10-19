package cn.iocoder.yudao.module.pms.dal.dataobject.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeContentLevelEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 知识内容协作权限 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_content_permission")
@KeySequence("pms_knowledge_content_permission_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeContentPermissionDO extends BaseDO {

    /**
     * 内容协作权限编号
     */
    @TableId
    private Long id;
    /**
     * 知识库编号
     *
     * 关联 {@link PmsKnowledgeLibraryDO#getId()}
     */
    private Long libraryId;
    /**
     * 是否对知识库可访问人公开
     */
    private Boolean openStatus;
    /**
     * 公开协作等级
     *
     * 枚举 {@link PmsKnowledgeContentLevelEnum}
     */
    private Integer openLevel;

}
