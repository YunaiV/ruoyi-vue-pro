package cn.iocoder.yudao.module.promotion.controller.admin.decorate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentSaveReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentRespVO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageEnum;
import cn.iocoder.yudao.module.promotion.service.decorate.DecorateComponentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.promotion.convert.decorate.DecorateComponentConvert.INSTANCE;

@Tag(name = "管理后台 - 店铺页面装修")
@RestController
@RequestMapping("/promotion/decorate")
@Validated
public class DecorateComponentController {
    @Resource
    private DecorateComponentService decorateComponentService;

    @PostMapping("/page-save")
    @Operation(summary = "保存页面装修")
    // TODO 加权限
    public CommonResult<Boolean> savePageComponents(@Valid @RequestBody DecorateComponentSaveReqVO reqVO) {
        decorateComponentService.savePageComponents(reqVO);
        return success(true);
    }

    @GetMapping("/get-page-components")
    @Operation(summary = "获取装修页面组件")
    @Parameter(name = "pageId", description = "页面 id", required = true)
    // TODO 加权限
    public CommonResult<DecorateComponentRespVO> getPageComponents(
            @RequestParam("pageId") @InEnum(DecoratePageEnum.class) Integer pageId) {
        return success(INSTANCE.convert2(pageId, decorateComponentService.getPageComponents(pageId)));
    }

}
