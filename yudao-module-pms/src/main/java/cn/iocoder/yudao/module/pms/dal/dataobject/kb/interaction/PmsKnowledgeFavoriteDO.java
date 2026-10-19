package cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 知识收藏（关注）DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_favorite")
@KeySequence("pms_knowledge_favorite_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeFavoriteDO extends BaseDO {

    /**
     * 收藏编号
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
     * 收藏对象类型
     *
     * 枚举 {@link PmsKnowledgeObjectTypeEnum}
     */
    private Integer type;
    /**
     * 收藏对象编号
     *
     * 根据 {@link #getType()} 关联知识库、文件夹或文档编号
     */
    private Long entityId;
    /**
     * 收藏人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long userId;

}
