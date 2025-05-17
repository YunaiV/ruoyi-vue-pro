package cn.iocoder.yudao.module.pay.api.transfer.dto;

import lombok.Data;

/**
 * 转账单创建 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PayTransferCreateRespDTO {

    /**
     * 编号
     */
    private Long id;

    // ========== 其它字段 ==========

    /**
     * 渠道 package 信息
     *
     * 特殊：目前只有微信转账有这个东西！！！
     * @see <a href="https://pay.weixin.qq.com/doc/v3/merchant/4012716430">JSAPI 调起用户确认收款</a>
     */
    private String channelPackageInfo;

}
