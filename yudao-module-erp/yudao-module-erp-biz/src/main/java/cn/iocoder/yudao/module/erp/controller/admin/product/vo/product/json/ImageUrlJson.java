package cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.json;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @className: ImageUrlJson
 * @author: Wqh
 * @date: 2024/10/21 14:09
 * @Version: 1.0
 * @description:
 */
@Data
@AllArgsConstructor
public class ImageUrlJson implements Serializable {
    /**
     * key(图片简述)
     **/
    @NotBlank(message = "图片简述不能为空")
    private String key;
    /**
     * 图片url
     **/
    @NotBlank(message = "图片url不能为空")
    private String url;
    /**
     * 排序
     **/
    private Integer sort;
}
