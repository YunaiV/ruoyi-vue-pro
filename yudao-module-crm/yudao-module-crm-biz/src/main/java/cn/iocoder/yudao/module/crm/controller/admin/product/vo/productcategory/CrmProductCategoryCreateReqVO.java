package cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 产品分类创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmProductCategoryCreateReqVO extends CrmProductCategoryBaseVO {

}
