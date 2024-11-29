package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json;

import jakarta.validation.constraints.NotBlank;
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
     * key（价格简述）
     **/
    @NotBlank(message = "价格简述不能为空")
    private String key;
    /**
     * 价格
     **/
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    /**
     * 排序
     **/
    private Integer sort;
}

