package cn.iocoder.yudao.module.infra.enums.codegen;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码生成器的字段 HTML 展示枚举
 */
@AllArgsConstructor
@Getter
public enum CodegenColumnHtmlTypeEnum {

    INPUT("input"), // 文本框
    TEXTAREA("textarea"), // 文本域
    SELECT("select"), // 下拉框
    RADIO("radio"), // 单选框
    CHECKBOX("checkbox"), // 复选框
    DATETIME("datetime"), // 日期控件
    IMAGE_UPLOAD("imageUpload"), // 上传图片
    FILE_UPLOAD("fileUpload"), // 上传文件
    EDITOR("editor"), // 富文本控件
    ;

    /**
     * 条件
     */
    private final String type;

}
