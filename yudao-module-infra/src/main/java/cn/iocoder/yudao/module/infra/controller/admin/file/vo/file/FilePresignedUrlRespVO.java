package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "管理后台 - 文件预签名地址 Response VO")
@Data
public class FilePresignedUrlRespVO {

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    private Long configId;

    @Schema(description = "文件上传 URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://example.invalid/upload/example.png?signature=EXAMPLE")
    private String uploadUrl;

    /**
     * 为什么要返回 url 字段？
     *
     * 前端上传完文件后，需要使用该 URL 进行访问
     */
    @Schema(description = "文件访问 URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://test.yudao.iocoder.cn/758d3a5387507358c7236de4c8f96de1c7f5097ff6a7722b34772fb7b76b140f.png")
    private String url;

    /**
     * 为什么要返回 path 字段？
     *
     * 前端上传完文件后，需要调用 createFile 记录下 path 路径
     */
    @Schema(description = "文件路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx.png")
    private String path;

}
