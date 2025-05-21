package cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template;

import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPagePropertyRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 装修模板属性 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiyTemplatePropertyRespVO extends DiyTemplateBaseVO {

    @Schema(description = "装修模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    private Long id;

    @Schema(description = "模板属性，JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String property;

    @Schema(description = "模板页面", requiredMode = Schema.RequiredMode.REQUIRED, example = "[]")
    private List<DiyPagePropertyRespVO> pages;

}
