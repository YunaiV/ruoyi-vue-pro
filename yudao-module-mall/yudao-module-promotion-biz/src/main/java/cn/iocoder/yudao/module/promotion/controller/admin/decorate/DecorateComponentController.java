package cn.iocoder.yudao.module.promotion.controller.admin.decorate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.decorate.vo.DecorateComponentSaveReqVO;
import cn.iocoder.yudao.module.promotion.convert.decorate.DecorateComponentConvert;
import cn.iocoder.yudao.module.promotion.enums.decorate.DecoratePageEnum;
import cn.iocoder.yudao.module.promotion.service.decorate.DecorateComponentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 店铺页面装修")
@RestController
@RequestMapping("/promotion/decorate")
@Validated
public class DecorateComponentController {

    @Resource
    private DecorateComponentService decorateComponentService;

    @PostMapping("/save")
    @Operation(summary = "保存页面装修组件")
    @PreAuthorize("@ss.hasPermission('promotion:decorate:save')")
    public CommonResult<Boolean> saveDecorateComponent(@Valid @RequestBody DecorateComponentSaveReqVO reqVO) {
        decorateComponentService.saveDecorateComponent(reqVO);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获取指定页面的组件列表")
    @Parameter(name = "page", description = "页面 id", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:decorate:query')")
    public CommonResult<List<DecorateComponentRespVO>> getDecorateComponentListByPage(
            @RequestParam("page") @InEnum(DecoratePageEnum.class) Integer page) {
        return success(DecorateComponentConvert.INSTANCE.convertList02(
                decorateComponentService.getDecorateComponentListByPage(page, null)));
    }

}
