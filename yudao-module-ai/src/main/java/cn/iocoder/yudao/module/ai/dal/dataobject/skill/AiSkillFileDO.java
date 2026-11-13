package cn.iocoder.yudao.module.ai.dal.dataobject.skill;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.enums.skill.AiSkillFileTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 技能文件 DO
 *
 * 以 parentId 组织为文件树，只保存文本文件；读取时由目录名、文件名拼出相对路径匹配
 *
 * @author 芋道源码
 */
@TableName("ai_skill_file")
@KeySequence("ai_skill_file_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSkillFileDO extends BaseDO {

    /**
     * 父目录编号：根级
     */
    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 固定文件名：技能入口文件
     */
    public static final String NAME_SKILL = "SKILL.md";

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 技能编号
     *
     * 关联 {@link AiSkillDO#getId()}
     */
    private Long skillId;
    /**
     * 父目录编号
     *
     * 关联 {@link AiSkillFileDO#getId()}；根级为 {@link #PARENT_ID_ROOT}
     */
    private Long parentId;
    /**
     * 文件或目录名称
     */
    private String name;
    /**
     * 类型
     *
     * 枚举 {@link AiSkillFileTypeEnum}
     */
    private Integer type;
    /**
     * 文件类型，即小写的扩展名；目录为空
     */
    private String fileType;
    /**
     * 文件内容；目录为空
     */
    private String content;
    /**
     * 文件大小，单位：字节（UTF-8）；目录为 0
     */
    private Integer size;
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
