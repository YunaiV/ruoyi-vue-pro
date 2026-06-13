package cn.iocoder.yudao.module.wms.dal.dataobject.md.merchant;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wms.enums.md.WmsMerchantTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 往来企业 DO
 *
 * @author 芋道源码
 */
@TableName("wms_merchant")
@KeySequence("wms_merchant_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsMerchantDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    /**
     * 往来企业编号
     */
    private String code;
    /**
     * 往来企业名称
     */
    private String name;
    /**
     * 往来企业类型
     *
     * 枚举 {@link WmsMerchantTypeEnum}
     */
    private Integer type;
    /**
     * 级别
     */
    private String level;
    /**
     * 开户行
     */
    private String bankName;
    /**
     * 银行账户
     */
    private String bankAccount;
    /**
     * 地址
     */
    private String address;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 座机号
     */
    private String telephone;
    /**
     * 联系人
     */
    private String contact;
    /**
     * Email
     */
    private String email;
    /**
     * 备注
     */
    private String remark;

}
