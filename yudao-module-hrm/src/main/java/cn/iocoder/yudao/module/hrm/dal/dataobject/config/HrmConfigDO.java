package cn.iocoder.yudao.module.hrm.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.hrm.enums.config.HrmConfigTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * HRM 通用业务配置 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_config")
@KeySequence("hrm_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 配置类型
     *
     * 枚举 {@link HrmConfigTypeEnum}
     */
    private Integer type;
    /**
     * 配置值
     */
    private String value;
    /**
     * 显示顺序
     */
    private Integer sort;

}
