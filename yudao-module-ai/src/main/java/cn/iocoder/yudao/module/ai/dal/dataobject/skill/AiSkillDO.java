package cn.iocoder.yudao.module.ai.dal.dataobject.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 技能 DO
 *
 * 由管理员维护，角色通过 {@link cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO#getSkillIds()} 绑定，
 * 对话时由模型按需只读读取，不物化到磁盘、不执行脚本
 *
 * @author 芋道源码
 */
@TableName("ai_skill")
@KeySequence("ai_skill_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSkillDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 技能名称
     *
     * 租户内唯一，创建后不可修改；仅支持小写字母、数字、中划线，必须与 SKILL.md 头信息的 name 一致
     */
    private String name;
    /**
     * 技能描述
     *
     * 以 SKILL.md 头信息的 description 为准，保存 SKILL.md 时同步；模型首先看到的唯一内容
     */
    private String description;
    /**
     * 版本，仅作文本标记
     */
    private String version;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
