package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 海关分类子表 渠道精简 Response VO")
@Data
public class ErpCustomCategoryItemSimpleRespVO {

    private Long customCategoryId;

//    /**
//     * 材质-字典
//     */
//    private Integer material;
//    /**
//     * 报关品名
//     */
//    private String declaredType;

    /**
     * 材质对应string+报关品名
     */
    private String combinedValue;
}
