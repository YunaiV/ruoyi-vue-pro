package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity;

import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product.BargainProductCreateReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import java.util.List;

@Schema(description = "管理后台 - 砍价活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainActivityCreateReqVO extends BargainActivityBaseVO {

    @Schema(description = "砍价商品", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<BargainProductCreateReqVO> products;

}
