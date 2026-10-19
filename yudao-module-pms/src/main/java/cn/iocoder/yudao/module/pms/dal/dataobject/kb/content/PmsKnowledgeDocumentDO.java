package cn.iocoder.yudao.module.pms.dal.dataobject.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.LongListTypeHandler;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentStatusEnum;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PMS 知识库文档 DO
 *
 * @author 芋道源码
 */
@TableName(value = "pms_knowledge_document", autoResultMap = true)
@KeySequence("pms_knowledge_document_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeDocumentDO extends BaseDO {

    /**
     * 根文件夹编号
     */
    public static final Long FOLDER_ID_ROOT = 0L;
    /**
     * 根文档编号
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 文档编号
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
     * 内容协作权限编号
     *
     * 关联 {@link PmsKnowledgeContentPermissionDO#getId()}
     */
    private Long permissionId;
    /**
     * 文件夹编号，0 表示不在文件夹中
     *
     * 关联 {@link PmsKnowledgeFolderDO#getId()}
     */
    private Long folderId;
    /**
     * 父文档编号，0 表示根文档
     *
     * 关联 {@link #getId()}
     */
    private Long parentId;
    /**
     * 文档标题
     */
    private String title;
    /**
     * 文档内容或文件地址
     */
    private String content;
    /**
     * 文档类型
     *
     * 枚举 {@link PmsKnowledgeDocumentTypeEnum}
     */
    private Integer type;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;
    /**
     * 文档状态
     *
     * 枚举 {@link PmsKnowledgeDocumentStatusEnum}
     */
    private Integer status;
    /**
     * 标签编号列表
     *
     * 关联 {@link PmsKnowledgeDocumentLabelDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> labelIds;
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
