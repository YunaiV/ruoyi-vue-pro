package cn.iocoder.yudao.module.oa.controller.admin.file.vo.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 云盘空间 Response VO")
@Data
public class OaFileStorageRespVO {

    @Schema(description = "已用容量，单位字节", example = "1024")
    private Long usedSize;

    @Schema(description = "总容量，单位字节", example = "1073741824")
    private Long totalSize;

    @Schema(description = "本人正常文件数量，不含目录和回收站", example = "1")
    private Long fileCount;

    @Schema(description = "本人有效共享节点数量，按文件或目录去重", example = "1")
    private Long sharedCount;

    @Schema(description = "收到的共享入口数量，不重复统计子节点", example = "1")
    private Long receivedCount;

}
