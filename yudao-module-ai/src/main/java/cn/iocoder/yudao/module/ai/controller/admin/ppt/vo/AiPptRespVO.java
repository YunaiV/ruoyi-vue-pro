package cn.iocoder.yudao.module.ai.controller.admin.ppt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - AI PPT Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - AI PPT Response VO")
@Data
public class AiPptRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "新能源汽车市场分析")
    private String title;

    @Schema(description = "素材来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer source;

    @Schema(description = "大纲或材料文本", example = "# 新能源汽车市场分析")
    private String content;

    @Schema(description = "上传材料文件地址", example = "https://www.iocoder.cn/report.pdf")
    private String fileUrl;

    @Schema(description = "风格与设计要求", example = "极简商务风")
    private String prompt;

    @Schema(description = "格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer mode;

    @Schema(description = "语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "zh-CN")
    private String language;

    @Schema(description = "请求页数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer pageCount;

    @Schema(description = "对话模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long chatModelId;

    @Schema(description = "绘图模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Long imageModelId;

    @Schema(description = "分镜与配图")
    private List<Slide> slides;

    @Schema(description = "封面图地址", example = "https://www.iocoder.cn/ppt/1.png")
    private String coverUrl;

    @Schema(description = "分镜页数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer slideCount;

    @Schema(description = "已有配图页数", requiredMode = Schema.RequiredMode.REQUIRED, example = "6")
    private Integer finishCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "失败原因", example = "第 7 页：模型返回内容为空")
    private String errorMessage;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "幻灯片（一页）")
    @Data
    public static class Slide {

        @Schema(description = "页码，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer index;

        @Schema(description = "本页主题", example = "市场")
        private String theme;

        @Schema(description = "本页标题", example = "市场规模与增速")
        private String title;

        @Schema(description = "本页要点")
        private List<String> points;

        @Schema(description = "首次生图的提示词", example = "16:9 幻灯片，左侧大标题文字……")
        private String imagePrompt;

        @Schema(description = "当前图地址，为空表示本页还没有图", example = "https://www.iocoder.cn/ppt/1.png")
        private String imageUrl;

        @Schema(description = "本页最近一次生成的状态，为空表示待生成", example = "20")
        private Integer status;

        @Schema(description = "本页最近一次失败的原因", example = "模型返回内容为空")
        private String errorMessage;

        @Schema(description = "历史版本")
        private List<SlideImage> images;

    }

    @Schema(description = "幻灯片的一个图片版本")
    @Data
    public static class SlideImage {

        @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "https://www.iocoder.cn/ppt/1.png")
        private String url;

        @Schema(description = "实际发给绘图模型的完整提示词", requiredMode = Schema.RequiredMode.REQUIRED)
        private String prompt;

        @Schema(description = "生成时间", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDateTime createTime;

    }

}
