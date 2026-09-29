package cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 知识库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1KnowledgeBasePageReqVO extends PageParam {

    @Schema(description = "名称，模糊匹配", example = "产品手册")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
