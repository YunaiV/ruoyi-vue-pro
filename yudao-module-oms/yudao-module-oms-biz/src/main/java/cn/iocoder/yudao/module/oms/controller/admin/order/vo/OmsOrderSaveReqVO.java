package cn.iocoder.yudao.module.oms.controller.admin.order.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - OMS订单新增/修改 Request VO")
@Data
public class OmsOrderSaveReqVO {

    /**
     * 主键
     */
    @TableId
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

}