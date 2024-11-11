package cn.iocoder.yudao.module.erp.api.product.dto;

import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import lombok.Data;

/**
 * @className: ErpProductDTO
 * @author: Wqh
 * @date: 2024/11/5 10:37
 * @Version: 1.0
 * @description:
 */
@Data
public class ErpProductDTO {
    /**
     * 海关规则id  =====>>>> 映射到pd_declaration_statement
     */
    private String ruleId;
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
    private String productTitle;

    /**
     * 产品名称英文名
     */
    private String productTitleEn;

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
    private Float pdNetWeight;

    /**
     * 长
     */
    private Float pdNetLength;

    /**
     * 宽
     */
    private Float pdNetWidth;

    /**
     * 高
     */
    private Float pdNetHeight;

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
     * 材料（英文）
     */
    private String materialEn;

    /**
     * 默认采购单价
     */
    private Float productPurchaseValue;

    /**
     * 采购货币币种
     */
    private String currencyCode;

    /**
     * 默认供应商代码
     */
    private String defaultSupplierCode;

    /**
     * 产品销售状态
     */
    private Integer saleStatus;

    /**
     * 产品物流属性
     */
    private String logisticAttribute;

    /**
     * 海关编码
     */
    private String hsCode;

    /**
     * 申报价值
     */
    private Float productDeclaredValue;

    /**
     * 申报币种
     */
    private String pdDeclareCurrencyCode;

    /**
     * 申报品名CN
     */
    private String pdOverseaTypeCn;

    /**
     * 申报品名EN
     */
    private String pdOverseaTypeEn;

    /**
     * 清关税率
     */
    private Float fboTaxRate;

    /**
     * 一级品类名称
     */
    private Integer productCategoryId1;

    /**
     * 二级品类名称
     */
    private Integer productCategoryId2;

    /**
     * 三级品类名称
     */
    private Integer productCategoryId3;

    /**
     * 产品创建人的部门id{从数据库查询出来的是部门的名称，最后在上传eccang的时候转换成了eccang的组织id}
     */
    private String userOrganizationId;

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
