package cn.iocoder.yudao.module.bpm.service.definition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Bpm 表单的 Field 表单项 Response DTO
 * 字段的定义，可见 https://github.com/JakHuang/form-generator/issues/46 文档
 *
 * @author 芋道源码
 */
@Data
public class BpmFormFieldRespDTO {

    /**
     * 表单标题
     */
    private String label;
    /**
     * 表单字段的属性名，可自定义
     */
    @JsonProperty(value = "vModel")
    private String vModel;

}
