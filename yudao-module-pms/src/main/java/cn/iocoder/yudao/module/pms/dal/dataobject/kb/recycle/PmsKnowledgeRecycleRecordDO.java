package cn.iocoder.yudao.module.pms.dal.dataobject.kb.recycle;

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

import java.time.LocalDateTime;

/**
 * PMS 知识库回收站记录 DO
 *
 * @author 芋道源码
 */
@TableName("pms_knowledge_recycle_record")
@KeySequence("pms_knowledge_recycle_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsKnowledgeRecycleRecordDO extends BaseDO {

    /**
     * 回收站记录编号
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
     * 回收对象类型
     *
     * 枚举 {@link PmsKnowledgeObjectTypeEnum}
     */
    private Integer type;
    /**
     * 回收对象编号
     *
     * 根据 {@link #getType()} 关联知识库、文件夹或文档编号
     */
    private Long entityId;
    /**
     * 回收对象名称
     */
    private String name;
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
