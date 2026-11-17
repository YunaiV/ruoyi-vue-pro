package cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 短剧公开作品 Response VO：只包含白名单字段，不返回剧本正文、提示词与资产配置")
@Data
public class AiDramaPublicRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "作品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "末班地铁")
    private String name;

    @Schema(description = "画风", example = "2")
    private Integer style;

    @Schema(description = "画幅：成片实际使用的画幅", example = "9:16")
    private String aspectRatio;

    @Schema(description = "成片地址：用于播放", example = "https://www.iocoder.cn/1.mp4")
    private String composeFileUrl;

    @Schema(description = "成片时长（秒）", example = "42.5")
    private Double composeDuration;

    @Schema(description = "镜头数：成片实际拼接的镜头数", example = "8")
    private Integer storyboardCount;

    @Schema(description = "作者编号", example = "1")
    private Long userId;

    @Schema(description = "作者昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "是否本人的作品", example = "true")
    private Boolean owner;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // ========== 详情才返回 ==========

    @Schema(description = "剧情简介：取剧本大纲", example = "……")
    private String outline;

    @Schema(description = "出场角色")
    private List<Cast> characters;

    @Schema(description = "出场角色")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cast {

        @Schema(description = "角色名称", example = "林夏")
        private String name;

        @Schema(description = "选用的形象图", example = "https://www.iocoder.cn/1.png")
        private String imageUrl;

    }

}
