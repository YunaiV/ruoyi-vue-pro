package cn.iocoder.yudao.module.erp.service.logistic.category.bo;

public class ErpCustomCategoryBO {
    /**
     * 编号
     */
    private Long id;
    /**
     * 材质-字典
     */
    private Integer material;
    /**
     * 报关品名
     */
    private String declaredType;
    /**
     * 英文品名
     */
    private String declaredTypeEn;
    /**
     * 材质对应string+报关品名(动态计算)
     */
    private String combinedValue;
}
