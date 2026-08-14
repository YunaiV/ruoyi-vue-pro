package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliarytype.FmsAuxiliaryTypeRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliarytype.FmsAuxiliaryTypeSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 辅助核算类别")
@RestController
@RequestMapping("/fms/config/auxiliary-type")
@Validated
public class FmsAuxiliaryTypeController {

    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建辅助核算类别")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:create')")
    public CommonResult<Long> createAuxiliaryType(@Valid @RequestBody FmsAuxiliaryTypeSaveReqVO createReqVO) {
        return success(auxiliaryTypeService.createAuxiliaryType(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新辅助核算类别")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:update')")
    public CommonResult<Boolean> updateAuxiliaryType(@Valid @RequestBody FmsAuxiliaryTypeSaveReqVO updateReqVO) {
        auxiliaryTypeService.updateAuxiliaryType(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除辅助核算类别")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "辅助核算类别编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:delete')")
    public CommonResult<Boolean> deleteAuxiliaryType(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("id") @NotNull Long id) {
        auxiliaryTypeService.deleteAuxiliaryType(accountSetId, id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得辅助核算类别列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:query')")
    public CommonResult<List<FmsAuxiliaryTypeRespVO>> getAuxiliaryTypeList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        return success(BeanUtils.toBean(auxiliaryTypeService.getAuxiliaryTypeList(
                accountSetId, getLoginUserId()), FmsAuxiliaryTypeRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得辅助核算类别精简列表", description = "主要用于前端的下拉选项")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    public CommonResult<List<FmsAuxiliaryTypeRespVO>> getAuxiliaryTypeSimpleList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsAuxiliaryTypeDO> list = auxiliaryTypeService.getAuxiliaryTypeList(
                accountSetId, getLoginUserId());
        return success(convertList(list, auxiliaryType -> new FmsAuxiliaryTypeRespVO()
                .setId(auxiliaryType.getId()).setName(auxiliaryType.getName()).setType(auxiliaryType.getType())));
    }

}
