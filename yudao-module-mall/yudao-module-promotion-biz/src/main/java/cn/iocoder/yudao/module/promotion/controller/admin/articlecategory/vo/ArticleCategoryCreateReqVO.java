package cn.iocoder.yudao.module.promotion.controller.admin.articlecategory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 文章分类创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ArticleCategoryCreateReqVO extends ArticleCategoryBaseVO {

}
