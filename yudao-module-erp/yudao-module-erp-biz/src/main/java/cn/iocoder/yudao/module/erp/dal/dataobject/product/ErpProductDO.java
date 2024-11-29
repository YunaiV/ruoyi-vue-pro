package cn.iocoder.yudao.module.erp.dal.dataobject.product;

import lombok.*;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * ERP 产品 DO
 *
 * @author 王奇辉
 */
@TableName("erp_product")
@KeySequence("erp_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErpProductDO extends BaseDO {

    /**
     * 产品编号
     */
    @TableId
    private Long id;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 产品分类编号
     */
    private Long categoryId;
    /**
     * 部门id
     */
    private Long deptId;
    /**
     * SKU（编码）
     */
    private String barCode;
    /**
     * 单位编号
     */
    private Long unitId;
    /**
     * 材料（中文）
     */
    private String material;
    /**
     * 产品状态（1启用，0禁用）
     */
    private Boolean status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 基础重量（kg）
     */
    private BigDecimal weight;
    /**
     * 系列
     */
    private String series;
    /**
     * 颜色
     */
    private String color;
    /**
     * 型号
     */
    private String model;
    /**
     * 流水号
     */
    private Integer serial;
    /**
     * 生产编号
     */
    private String productionNo;
    /**
     * 基础宽度（mm）
     */
    private BigDecimal width;
    /**
     * 基础长度（mm）
     */
    private BigDecimal length;
    /**
     * 基础高度（mm）
     */
    private BigDecimal height;
    /**
     * 图片url
     */
    private String imageUrl;
    /**
     * 指导价，json格式
     */
    private String guidePrice;
    /**
     * 专利
     */
    private String patent;
    /**
     * PO产品经理id
     */
    private Long poId;
    /**
     * ID工业设计id
     */
    private Long idId;
    /**
     * RD研发工程师id
     */
    private Long rdId;
    /**
     * 维护工程师id
     */
    private Long meId;
    /**
     * 层板承重
     */
    private String shelfLoadCapacity;
    /**
     * 层板数量
     */
    private Integer shelvesCount;
    /**
     * 电视调节方式
     */
    private String tvAdjustmentMethod;
    /**
     * 层板调节方式
     */
    private String shelfAdjustmentMethod;
    /**
     * 设计说明
     */
    private String description;
    /**
     * 宽度最大值
     */
    private BigDecimal widthMax;
    /**
     * 宽度最小值
     */
    private BigDecimal widthMin;
    /**
     * 长度最大值
     */
    private BigDecimal lengthMax;
    /**
     * 长度最小值
     */
    private BigDecimal lengthMin;

}