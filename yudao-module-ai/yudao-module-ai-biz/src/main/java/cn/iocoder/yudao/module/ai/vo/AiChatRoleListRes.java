package cn.iocoder.yudao.module.ai.vo;

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
public class AiChatRoleListRes {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "模型id")
    private String modelId;

    @Schema(description = "角色名字")
    private String roleName;

    @Schema(description = "角色介绍，详细描述角色的功能或用途")
    private String roleIntroduce;

    @Schema(description = "角色来源，如 system（系统预置）、customer（用户自定义）")
    private String roleSource;

    @Schema(description = "分类，角色所属的类别，如娱乐、创作等")
    private String classify;

    @Schema(description = "发布状态，0表示仅自己可见，1表示公开，2表示禁用")
    private String visibility;

    @Schema(description = "生成时的Top-K采样候选集大小")
    private Double topK;

    @Schema(description = "生成时使用的核采样方法的概率阈值")
    private Double topP;

    @Schema(description = "用于控制随机性和多样性的温度参数")
    private Double temperature;

    @Schema(description = "角色的使用次数统计")
    private Integer useCount;
}
