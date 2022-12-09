package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(title = "管理后台 - 规格 + 规格值 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyAndValueRespVO extends ProductPropertyBaseVO {

    @Schema(title = "规格的编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

    /**
     * 规格值的集合
     */
    private List<ProductPropertyValueRespVO> values;

}
