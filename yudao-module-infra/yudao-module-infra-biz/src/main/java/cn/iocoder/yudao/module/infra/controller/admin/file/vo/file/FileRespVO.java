package cn.iocoder.yudao.module.infra.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 文件 Response VO", description = "不返回 content 字段，太大")
@Data
public class FileRespVO {

    @Schema(title = "文件编号", required = true, example = "1024")
    private Long id;

    @Schema(title = "配置编号", required = true, example = "11")
    private Long configId;

    @Schema(title = "文件路径", required = true, example = "yudao.jpg")
    private String path;

    @Schema(title = "原文件名", required = true, example = "yudao.jpg")
    private String name;

    @Schema(title = "文件 URL", required = true, example = "https://www.iocoder.cn/yudao.jpg")
    private String url;

    @Schema(title = "文件MIME类型", example = "application/octet-stream")
    private String type;

    @Schema(title = "文件大小", example = "2048", required = true)
    private Integer size;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

}
