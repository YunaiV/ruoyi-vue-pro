package cn.iocoder.yudao.adminserver.modules.pay.dal.dataobject.merchant;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 支付应用 DO
 * 一个商户下，可能会有多个支付应用。例如说，京东有京东商城、京东到家等等
 * 不过一般来说，一个商户，只有一个应用哈~
 *
 * 即 PayMerchantDO : PayAppDO = 1 : n
 *
 * @author 芋道源码
 */
@Data
public class PayAppDO extends BaseDO {

    /**
     * 应用编号，数据库自增
     */
    @TableId
    private Long id;
    /**
     * 应用号
     * 例如说，60cc81e0e4b06afc4d3f0cfq
     */
    private String no;
    /**
     * 应用名
     */
    private String name;
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
    /**
     * 应用私钥
     * TODO 芋艿：用途
     */
    private String secret;

    /**
     * 商户编号
     *
     * 关联 {@link PayMerchantDO#getId()}
     */
    private Long merchantId;

}
