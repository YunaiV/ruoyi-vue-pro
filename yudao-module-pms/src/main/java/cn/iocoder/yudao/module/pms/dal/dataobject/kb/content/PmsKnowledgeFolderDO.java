package cn.iocoder.yudao.module.pms.dal.dataobject.kb.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeContentPermissionDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryDO;
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
 * PMS 知识库文件夹 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_folder")
@KeySequence("pms_knowledge_folder_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeFolderDO extends BaseDO {

    /**
     * 根文件夹编号
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 文件夹编号
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
     * 父文件夹编号，0 表示根目录
     *
     * 关联 {@link #getId()}
     */
    private Long parentId;
    /**
     * 文件夹标题
     */
    private String title;
    /**
     * 文件夹状态
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
