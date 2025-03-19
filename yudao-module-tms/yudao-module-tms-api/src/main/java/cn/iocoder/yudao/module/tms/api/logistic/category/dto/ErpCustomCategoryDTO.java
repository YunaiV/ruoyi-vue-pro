package cn.iocoder.yudao.module.tms.api.logistic.category.dto;

import lombok.Data;

@Data
public class ErpCustomCategoryDTO {
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
}
