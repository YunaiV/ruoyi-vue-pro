package cn.iocoder.yudao.module.pms.dal.dataobject.kb.library;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * PMS 知识库模板 DO
 *
 * <p>模板只保存新建知识库时使用的默认文档，知识库创建后会生成独立的文档内容</p>
 *
 * @author 芋道源码
 */
@TableName(value = "pms_knowledge_library_template", autoResultMap = true)
@KeySequence("pms_knowledge_library_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeLibraryTemplateDO extends BaseDO {

    /**
     * 模板编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板简介
     */
    private String description;
    /**
     * 模板封面地址
     */
    private String coverUrl;
    /**
     * 模板状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 模板文档列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Document> documents;

    /**
     * 模板文档
     */
    @Data
    public static class Document {

        /**
         * 文档标题
         */
        private String title;
        /**
         * 文档内容
         */
        private String content;

    }

}
