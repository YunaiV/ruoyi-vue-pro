package cn.iocoder.yudao.module.mes.dal.dataobject.pro.andon;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mes.enums.pro.MesProAndonLevelEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;

/**
 * MES 安灯呼叫配置 DO
 *
 * @author 芋道源码
 */
@TableName("mes_pro_andon_config")
@KeySequence("mes_pro_andon_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesProAndonConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 呼叫原因
     */
    private String reason;
    /**
     * 级别
     *
     * 字典 {@link DictTypeConstants#MES_PRO_ANDON_LEVEL}
     * 枚举 {@link MesProAndonLevelEnum}
     */
    private Integer level;
    /**
     * 处置人角色编号
     *
     * 关联 system_role#id
     */
    private Long handlerRoleId;
    /**
     * 处置人编号
     *
     * 关联 system_users#id
     */
    private Long handlerUserId;
    /**
     * 备注
     */
    private String remark;

}
