package cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.json;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @className: GuidePriceJson
 * @author: Wqh
 * @date: 2024/10/21 14:11
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
public class GuidePriceJson implements Serializable {
    /**
     * 国别代码
     **/
    @NotNull(message = "国别代码")
    private Integer code;
    /**
     * 价格
     **/
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
}

