package cn.iocoder.yudao.module.ai1.dal.dataobject.skill;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.ai1.enums.skill.Ai1SkillFileTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI1 SKILL 内容文件 DO
 *
 * 以 parentId 组织为文件树，物化时按树结构写入本地技能目录
 *
 * @author 芋道源码
 */
@TableName("ai1_skill_file")
@KeySequence("ai1_skill_file_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1SkillFileDO extends TenantBaseDO {

    /**
     * 父目录编号：根级
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 固定文件名：SKILL 入口文件
     */
    public static final String NAME_SKILL = "SKILL.md";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * SKILL 编号
     *
     * 关联 {@link Ai1SkillDO#getId()}
     */
    private Long skillId;
    /**
     * 父目录编号
     *
     * 关联 {@link Ai1SkillFileDO#getId()}；根级为 {@link #PARENT_ID_ROOT}
     */
    private Long parentId;
    /**
     * 文件或目录名称
     */
    private String name;
    /**
     * 类型
     *
     * 枚举 {@link Ai1SkillFileTypeEnum}
     */
    private Integer type;
    /**
     * 文件类型，即扩展名；目录为空
     */
    private String fileType;
    /**
     * 文件内容；目录为空
     */
    private String content;
    /**
     * 是否固定
     *
     * 固定节点（SKILL.md、scripts/、reference/）不可删除、改名、移动
     */
    private Boolean locked;
    /**
     * 排序
     */
    private Integer sort;

}
