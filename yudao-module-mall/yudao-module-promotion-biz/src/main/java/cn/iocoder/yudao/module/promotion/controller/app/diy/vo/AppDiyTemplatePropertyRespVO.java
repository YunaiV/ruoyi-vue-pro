package cn.iocoder.yudao.module.promotion.controller.app.diy.vo;

import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPagePropertyRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Schema(description = "用户 App - 装修模板属性 Response VO")
@Data
@ToString(callSuper = true)
public class AppDiyTemplatePropertyRespVO {

    @Schema(description = "装修模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    private Long id;

    @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认主题")
    private String name;

    @Schema(description = "模板属性，JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String property;

    @Schema(description = "模板页面", requiredMode = Schema.RequiredMode.REQUIRED, example = "[]")
    private List<DiyPagePropertyRespVO> pages;

}
