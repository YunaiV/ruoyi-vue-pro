package cn.iocoder.yudao.module.ai.controller.admin.chat.vo.message;

import cn.iocoder.yudao.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 聊天消息 Response VO")
@Data
public class AiChatMessageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long conversationId;

    @Schema(description = "回复消息编号", example = "1024")
    private Long replyId;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "role")
    private String type; // 参见 MessageType 枚举类

    @Schema(description = "用户编号", example = "4096")
    private Long userId;

    @Schema(description = "角色编号", example = "888")
    private Long roleId;

    @Schema(description = "模型标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "gpt-3.5-turbo")
    private String model;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long modelId;

    @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，你好啊")
    private String content;

    @Schema(description = "推理内容", example = "要达到这个目标，你需要...")
    private String reasoningContent;

    @Schema(description = "是否携带上下文", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean useContext;

    @Schema(description = "知识库段落编号数组", example = "[1,2,3]")
    private List<Long> segmentIds;

    @Schema(description = "知识库段落数组")
    private List<KnowledgeSegment> segments;

    @Schema(description = "联网搜索的网页内容数组")
    private List<AiWebSearchResponse.WebPage> webSearchPages;

    @Schema(description = "附件 URL 数组", example = "https://www.iocoder.cn/1.png")
    private List<String> attachmentUrls;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-05-12 12:51")
    private LocalDateTime createTime;

    // ========== 仅在【对话管理】时加载 ==========

    @Schema(description = "角色名字", example = "小黄")
    private String roleName;

    @Schema(description = "知识库段落", example = "Java 开发手册")
    @Data
    public static class KnowledgeSegment {

        @Schema(description = "段落编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "切片内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java 开发手册")
        private String content;

        @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24790")
        private Long documentId;

        @Schema(description = "文档名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品使用手册")
        private String documentName;

    }

}
