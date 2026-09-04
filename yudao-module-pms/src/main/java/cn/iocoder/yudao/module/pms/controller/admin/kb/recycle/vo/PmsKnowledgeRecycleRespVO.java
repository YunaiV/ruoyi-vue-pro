package cn.iocoder.yudao.module.pms.controller.admin.kb.recycle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 知识库回收站记录 Response VO")
@Data
public class PmsKnowledgeRecycleRespVO {

    @Schema(description = "回收站记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "知识库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long libraryId;

    @Schema(description = "对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer type;

    @Schema(description = "对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long entityId;

    @Schema(description = "对象名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品说明")
    private String name;

    @Schema(description = "文件大小，单位：字节")
    private Long fileSize;

    @Schema(description = "删除人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long deleteUserId;

    @Schema(description = "删除人姓名", example = "芋道")
    private String deleteUserName;

    @Schema(description = "删除时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime deleteTime;

    @Schema(description = "父对象编号，回收站详情中用于组装层级", example = "2048")
    private Long parentId;

    @Schema(description = "所属文件夹编号，回收站详情中用于组装层级", example = "2048")
    private Long folderId;

}
