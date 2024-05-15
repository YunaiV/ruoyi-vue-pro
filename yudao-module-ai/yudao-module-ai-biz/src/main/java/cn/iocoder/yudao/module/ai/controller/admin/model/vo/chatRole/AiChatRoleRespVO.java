package cn.iocoder.yudao.module.ai.controller.admin.model.vo.chatRole;

import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 聊天角色 Response VO")
@Data
public class AiChatRoleRespVO implements VO {

    @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32746")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9442")
    private Long userId;

    @Schema(description = "模型编号", example = "17640")
    @Trans(type = TransType.SIMPLE, target = AiChatModelDO.class, fields = {"name", "model"}, refs = {"modelName", "model"})
    private Long modelId;
    @Schema(description = "模型名字", example = "张三")
    private String modelName;
    @Schema(description = "模型标识", example = "gpt-3.5-turbo-0125")
    private String model;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    @Schema(description = "角色头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String avatar;

    @Schema(description = "角色类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "创作")
    private String category;

    @Schema(description = "角色排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "角色描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "你说的对")
    private String description;

    @Schema(description = "角色设定", requiredMode = Schema.RequiredMode.REQUIRED)
    private String systemMessage;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Boolean publicStatus;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}