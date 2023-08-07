package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO @puhui999：是不是应该把 BargainProductCreateReqVO 和 BargainProductUpdateReqVO 合并在一起哈。就是一个 SaveReqVO
@Schema(description = "管理后台 - 砍价商品创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainProductCreateReqVO extends BargainProductBaseVO {

}
