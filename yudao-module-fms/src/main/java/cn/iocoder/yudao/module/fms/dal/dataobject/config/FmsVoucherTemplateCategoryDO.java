package cn.iocoder.yudao.module.fms.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * FMS 凭证模板分类 DO
 *
 * @author 芋道源码
 */
@TableName("fms_voucher_template_category")
@KeySequence("fms_voucher_template_category_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsVoucherTemplateCategoryDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 凭证模板分类名称
     */
    private String name;
    /**
     * 账套编号
     *
     * 关联 {@link cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO#getId()}
     */
    private Long accountSetId;

}
