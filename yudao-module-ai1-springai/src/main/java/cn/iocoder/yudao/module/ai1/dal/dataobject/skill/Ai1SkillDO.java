package cn.iocoder.yudao.module.ai1.dal.dataobject.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI1 SKILL DO
 *
 * @author 芋道源码
 */
@TableName("ai1_skill")
@KeySequence("ai1_skill_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ai1SkillDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 名称
     *
     * 同时作为物化目录名，租户内唯一，仅支持字母、数字、中划线
     */
    private String name;
    /**
     * 描述
     */
    private String description;
    /**
     * 版本
     */
    private String version;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    // TODO @AI：不新建唯一索引，所以这个字段可以删除掉噢；
    /**
     * 删除时间
     *
     * 唯一键 (tenant_id, name, deleted_at) 的组成部分：未删除时为数据库默认的纪元值，逻辑删除前写入当前时间，
     * 使同名 SKILL 删除后可重建
     */
    private LocalDateTime deletedAt;

}
