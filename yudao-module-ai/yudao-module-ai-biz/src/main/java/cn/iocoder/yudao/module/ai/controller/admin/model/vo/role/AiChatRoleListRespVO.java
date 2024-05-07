package cn.iocoder.yudao.module.ai.controller.admin.model.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 角色列表
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class AiChatRoleListRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "角色名称", example = "小红书写作")
    private String name;

    @Schema(description = "角色头像", example = "http://...")
    private String avatar;

    @Schema(description = "角色分类", example = "writing")
    private String category;

    @Schema(description = "角色描述", example = "角色描述")
    private String description;

    @Schema(description = "角色欢迎语", example = "欢迎...")
    private String welcomeMessage;

    @Schema(description = "角色设定（消息）", example = "你是拥有丰富的小红书写作经验作者xxxx")
    private String systemMessage;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "模型编号", example = "1")
    private Long modelId;

    @Schema(description = "是否公开 true - 公开；false - 私有", example = "true")
    private Boolean publicStatus;

    @Schema(description = "排序值 asc", example = "1")
    private Integer sort;

    @Schema(description = "状态 0、开启 1、关闭", example = "1")
    private Integer status;
}
