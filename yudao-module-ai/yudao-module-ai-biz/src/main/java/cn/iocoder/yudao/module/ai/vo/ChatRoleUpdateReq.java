package cn.iocoder.yudao.module.ai.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * chat 角色 - 更新
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
public class ChatRoleUpdateReq extends PageParam {

    @NotNull
    @Schema(description = "模型编号，关联到角色使用的特定模型")
    private String modelId;

    @NotNull
    @Schema(description = "角色名，角色的显示名称")
    private String roleName;

    @NotNull
    @Schema(description = "角色介绍，详细描述角色的功能或用途")
    private String roleIntroduce;

    @NotNull
    @Schema(description = "角色来源，如 system（系统预置）、customer（用户自定义）")
    private String roleSource;

    @NotNull
    @Schema(description = "分类，角色所属的类别，如娱乐、创作等")
    private String classify;

    @NotNull
    @Schema(description = "发布状态，0表示仅自己可见，1表示公开，2表示禁用")
    private String visibility;

    @NotNull
    @Schema(description = "生成时的Top-K采样候选集大小")
    private Double topK;

    @NotNull
    @Schema(description = "生成时使用的核采样方法的概率阈值")
    private Double topP;

    @NotNull
    @Schema(description = "用于控制随机性和多样性的温度参数")
    private Double temperature;
}
