package cn.iocoder.yudao.module.pms.dal.dataobject.kb.library;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * PMS 知识库 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_library")
@KeySequence("pms_knowledge_library_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeLibraryDO extends BaseDO {

    /**
     * 知识库编号
     */
    @TableId
    private Long id;
    /**
     * 知识库名称
     */
    private String name;
    /**
     * 知识库简介
     */
    private String description;
    /**
     * 是否公开
     */
    private Boolean openStatus;
    /**
     * 知识库封面
     */
    private String coverUrl;
    /**
     * 知识库状态
     *
     * 枚举 {@link PmsKnowledgeDocumentStatusEnum}
     */
    private Integer status;
    /**
     * 删除人用户编号
     *
     * 关联 {@link AdminUserDO#getId()}
     */
    private Long deleteUserId;
    /**
     * 删除时间
     */
    private LocalDateTime deleteTime;

}
