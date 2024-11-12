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
    private String customRuleId;
    /**
     * 供应商产品编码
     */
    private String supplierProductCode;
    /**
     * 产品SKU: 供应商产品编码-国家代码
     */
    private String productSku;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 国家编码
     */
    private String countryCode;

    /**
     * 图片地址（当前只有一张）
     **/
    private String imageUrl;

    /**
     * 产品图片集合
     */
    //private String productImgUrlList;

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
    private Float length;

    /**
     * 基础宽度
     */
    private Float width;

    /**
     * 基础高度
     */
    private Float height;

    /**
     * 材料
     */
    private String material;

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
     * 从数据库查询出来的是部门的名称，最后在上传eccang的时候转换成了eccang的组织id
     */
    private String creatorDeptName;

    /**
     * 条形码
     */
    private String barCode;

    /**
     * 产品部门名id
     **/
    private Long productDeptId;
    /**
     * 产品部门名称  ======>>>>>  对应eccang的品类
     **/
    private String productDeptName;
    /**
     * 产品创建人
     **/
    private String creator;

}
