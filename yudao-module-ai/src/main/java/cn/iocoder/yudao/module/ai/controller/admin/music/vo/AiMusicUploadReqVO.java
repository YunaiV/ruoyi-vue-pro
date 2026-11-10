package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

// TODO @AI：直接使用 upload 接口，可以么？全局的，然后传递 url？不用这里传递 file
/**
 * AI 音乐上传 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI 音乐上传 Request VO")
@Data
public class AiMusicUploadReqVO {

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    @NotNull(message = "模型不能为空")
    private Long modelId;

    @Schema(description = "音频文件，只支持 mp3、wav", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "音频文件不能为空")
    private MultipartFile file;

    @Schema(description = "歌曲名称，默认取文件名", example = "夏日海边")
    @Size(max = 100, message = "歌曲名称不能超过 100 个字符")
    private String title;

}
