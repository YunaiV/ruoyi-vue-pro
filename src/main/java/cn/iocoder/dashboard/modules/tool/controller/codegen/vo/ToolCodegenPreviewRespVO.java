package cn.iocoder.dashboard.modules.tool.controller.codegen.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(value = "代码生成预览 Response VO", description ="注意，每个文件都是一个该对象")
@Data
public class ToolCodegenPreviewRespVO {

    @ApiModelProperty(value = "文件路径", required = true, example = "java/cn/iocoder/dashboard/modules/system/controller/test/SysTestDemoController.java")
    private String filePath;

    @ApiModelProperty(value = "代码", required = true, example = "Hello World")
    private String code;

}
