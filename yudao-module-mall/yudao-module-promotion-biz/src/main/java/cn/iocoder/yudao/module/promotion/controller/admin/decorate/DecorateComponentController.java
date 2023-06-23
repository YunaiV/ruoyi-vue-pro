package cn.iocoder.yudao.module.promotion.controller.admin.decorate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentRespVO;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageTypeEnum;
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
    @Operation(summary = "页面装修保存")
    // TODO 加权限
    public CommonResult<Boolean> pageSave(@Valid @RequestBody DecorateComponentReqVO reqVO) {
        decorateComponentService.pageSave(reqVO);
        return success(true);
    }

    @GetMapping("/get-page-components")
    @Operation(summary = "获取装修页面组件")
    @Parameter(name = "type", description = "页面类型", required = true)
    // TODO 加权限
    public CommonResult<DecorateComponentRespVO> getPageComponents(@RequestParam("type")
                                                                   @InEnum(DecoratePageTypeEnum.class) Integer type) {
        return success(INSTANCE.convert2(type, decorateComponentService.getPageComponents(type)));
    }
}
