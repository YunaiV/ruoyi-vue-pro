package cn.iocoder.yudao.module.promotion.controller.app.decorate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.controller.app.decorate.vo.AppDecorateComponentRespVO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageEnum;
import cn.iocoder.yudao.module.promotion.service.decorate.DecorateComponentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.promotion.convert.decorate.DecorateComponentConvert.INSTANCE;

@Tag(name = "用户 APP - 店铺装修")
@RestController
@RequestMapping("/promotion/decorate")
@Validated
public class AppDecorateController {

    @Resource
    private DecorateComponentService decorateComponentService;

    @GetMapping("/get-page-components")
    @Operation(summary = "获取装修页面组件")
    @Parameter(name = "pageId", description = "页面 id", required = true)
    public CommonResult<AppDecorateComponentRespVO> getPageComponents(
            @RequestParam("pageId") @InEnum(DecoratePageEnum.class) Integer pageId) {
        return success(INSTANCE.appConvert(pageId, decorateComponentService.getPageComponents(pageId)));
    }

}
