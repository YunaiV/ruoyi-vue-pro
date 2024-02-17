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

    @Schema(description = "文件上传 URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://s3.cn-south-1.qiniucs.com/ruoyi-vue-pro/758d3a5387507358c7236de4c8f96de1c7f5097ff6a7722b34772fb7b76b140f.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=3TvrJ70gl2Gt6IBe7_IZT1F6i_k0iMuRtyEv4EyS%2F20240217%2Fcn-south-1%2Fs3%2Faws4_request&X-Amz-Date=20240217T123222Z&X-Amz-Expires=600&X-Amz-SignedHeaders=host&X-Amz-Signature=a29f33770ab79bf523ccd4034d0752ac545f3c2a3b17baa1eb4e280cfdccfda5")
    private String uploadUrl;

    /**
     * 为什么要返回 url 字段？
     *
     * 前端上传完文件后，需要使用该 URL 进行访问
     */
    @Schema(description = "文件访问 URL", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "https://test.yudao.iocoder.cn/758d3a5387507358c7236de4c8f96de1c7f5097ff6a7722b34772fb7b76b140f.png")
    private String url;

}
