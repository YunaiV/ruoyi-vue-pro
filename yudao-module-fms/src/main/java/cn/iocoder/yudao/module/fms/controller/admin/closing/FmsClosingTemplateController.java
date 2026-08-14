package cn.iocoder.yudao.module.fms.controller.admin.closing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.closing.vo.FmsClosingTemplateSaveReqVO;
import cn.iocoder.yudao.module.fms.service.closing.FmsClosingTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 结账模板")
@RestController
@RequestMapping("/fms/closing/template")
@Validated
public class FmsClosingTemplateController {

    @Resource
    private FmsClosingTemplateService closingTemplateService;

    @GetMapping("/list")
    @Operation(summary = "获得结账模板列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:closing:query')")
    public CommonResult<List<FmsClosingTemplateRespVO>> getClosingTemplateList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        return success(closingTemplateService.getClosingTemplateList(accountSetId, getLoginUserId()));
    }

    @PostMapping("/create")
    @Operation(summary = "创建结账模板")
    @PreAuthorize("@ss.hasPermission('fms:closing:update')")
    public CommonResult<Long> createClosingTemplate(
            @Valid @RequestBody FmsClosingTemplateSaveReqVO createReqVO) {
        return success(closingTemplateService.createClosingTemplate(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新结账模板")
    @PreAuthorize("@ss.hasPermission('fms:closing:update')")
    public CommonResult<Boolean> updateClosingTemplate(
            @Valid @RequestBody FmsClosingTemplateSaveReqVO updateReqVO) {
        closingTemplateService.updateClosingTemplate(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除结账模板")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "结账模板编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('fms:closing:update')")
    public CommonResult<Boolean> deleteClosingTemplate(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                       @RequestParam("id") @NotNull Long id) {
        closingTemplateService.deleteClosingTemplate(accountSetId, id, getLoginUserId());
        return success(true);
    }

}
