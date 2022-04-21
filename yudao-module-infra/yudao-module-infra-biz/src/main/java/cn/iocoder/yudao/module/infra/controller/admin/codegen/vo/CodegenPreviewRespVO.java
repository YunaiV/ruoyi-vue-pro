package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "管理后台 - 代码生成预览 Response VO", description ="注意，每个文件都是一个该对象")
@Data
public class CodegenPreviewRespVO {

    @ApiModelProperty(value = "文件路径", required = true, example = "java/cn/iocoder/yudao/adminserver/modules/system/controller/test/SysTestDemoController.java")
    private String filePath;

    @ApiModelProperty(value = "代码", required = true, example = "Hello World")
    private String code;

}
