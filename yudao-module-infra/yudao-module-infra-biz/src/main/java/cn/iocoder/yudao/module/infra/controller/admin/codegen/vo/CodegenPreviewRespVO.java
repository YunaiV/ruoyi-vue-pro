package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 代码生成预览 Response VO,注意，每个文件都是一个该对象")
@Data
public class CodegenPreviewRespVO {

    @Schema(description = "文件路径", required = true, example = "java/cn/iocoder/yudao/adminserver/modules/system/controller/test/SysTestDemoController.java")
    private String filePath;

    @Schema(description = "代码", required = true, example = "Hello World")
    private String code;

}
