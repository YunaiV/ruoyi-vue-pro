package cn.iocoder.yudao.module.erp.api.product.dto;

import lombok.Data;

/**
 * @className: ErpProductDTO
 * @author: Wqh
 * @date: 2024/11/5 10:37
 * @Version: 1.0
 * @description:
 */
@Data
public class ErpCustomRuleDTO {
    /**
     * 海关规则id  =====>>>> 映射到pd_declaration_statement
     */
    private String id;
    /**
     * 供应商产品编码
     */
    private String supplierProductCode;


    /**
     * 产品名称
     */
    private String productName;

    /**
     * 国家编码
     */
    private String countryCode;

    /**
     * 图片地址（当前只有一张）
     **/
    private String productImageUrl;

    /**
     * 重量
     */
    private Float packageWeight;

    /**
     * 长
     */
    private Float packageLength;

    /**
     * 宽
     */
    private Float packageWidth;

    /**
     * 高
     */
    private Float packageHeight;

    /**
     * 净重
     */
    private Float productWeight;

    /**
     * 基础长度
     */
    private Float productLength;

    /**
     * 基础宽度
     */
    private Float productWidth;

    /**
     * 基础高度
     */
    private Float productHeight;

    /**
     * 材料
     */
    private String productMaterial;

    /**
     * 采购货币币种
     */
    private String purchasePriceCurrencyCode;

    /**
     * 产品物流属性
     */
    private String logisticAttribute;

    /**
     * 海关编码
     */
    private String hscode;

    /**
     * 申报价值
     */
    private Float declaredValue;

    /**
     * 申报币种
     */
    private String declaredValueCurrencyCode;

    /**
     * 申报品名CN
     */
    private String declaredType;

    /**
     * 申报品名EN
     */
    private String declaredTypeEn;

    /**
     * 清关税率
     */
    private Float taxRate;

    /**
     * 条形码
     */
    private String barCode;

    /**
     * 产品部门名id
     **/
    private Long productDeptId;

    /**
     * 产品创建人
     **/
    private String productCreatorId;

}
