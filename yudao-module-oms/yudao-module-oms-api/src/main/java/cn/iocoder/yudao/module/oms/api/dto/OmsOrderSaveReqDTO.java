package cn.iocoder.yudao.module.oms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OmsOrderSaveReqDTO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 所属平台
     */
    private String platformCode;
    /**
     * 订单编码
     */
    private String code;
    /**
     * 外部单据编码，即平台订单号 唯一标识
     */
    private String externalCode;
    /**
     * 店铺id
     */
    private Long shopId;
    /**
     * 运费
     */
    private BigDecimal shippingFee;
    /**
     * 总金额
     */
    private BigDecimal totalPrice;
    /**
     * 买家姓名
     */
    private String buyerName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 最迟送达时间
     */
    private LocalDateTime deliveryLatestTime;
    /**
     * 订单创建时间
     */
    private LocalDateTime orderCreateTime;
    /**
     * 付款时间
     */
    private LocalDateTime payTime;
    /**
     * 电话
     */
    private String phone;
    /**
     * 公司名
     */
    private String companyName;
    /**
     * 收件人国家
     */
    private String buyerCountryCode;

    /**
     * 收件人姓名
     */

    private String recipientName;
    /**
     * 收件人国家
     */
    private String recipientCountryCode;
    /**
     * 收件人省【或为州】
     */
    private String state;
    /**
     * 城市
     */
    private String city;
    /**
     * 区/县
     */
    private String district;
    /**
     * 外部来源原地址，用作备份
     */
    private String externalAddress;
    /**
     * 地址1
     */
    private String address1;

    /**
     * 地址2
     */
    private String address2;

    /**
     * 地址3
     */
    private String address3;
    /**
     * 门牌号
     */
    private String houseNo;
    /**
     * 邮编
     */
    private String postalCode;

    private List<OmsOrderItemSaveReqDTO> omsOrderItemSaveReqDTOList;
}
