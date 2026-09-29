package cn.iocoder.yudao.module.ai1.controller.admin.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.*;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AI1 模型")
@RestController
@RequestMapping("/ai1/model")
@Validated
public class Ai1ModelController {

    @Resource
    private Ai1ModelService modelService;

    @PostMapping("/create")
    @Operation(summary = "创建模型")
    @PreAuthorize("@ss.hasPermission('ai1:model:create')")
    public CommonResult<Long> createModel(@Valid @RequestBody Ai1ModelSaveReqVO createReqVO) {
        return success(modelService.createModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新模型")
    @PreAuthorize("@ss.hasPermission('ai1:model:update')")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody Ai1ModelSaveReqVO updateReqVO) {
        modelService.updateModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") Long id) {
        modelService.deleteModel(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除模型")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:model:delete')")
    public CommonResult<Boolean> deleteModelList(@RequestParam("ids") List<Long> ids) {
        modelService.deleteModelListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:model:query')")
    public CommonResult<Ai1ModelRespVO> getModel(@RequestParam("id") Long id) {
        Ai1ModelDO model = modelService.getModel(id);
        return success(BeanUtils.toBean(model, Ai1ModelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得模型分页")
    @PreAuthorize("@ss.hasPermission('ai1:model:query')")
    public CommonResult<PageResult<Ai1ModelRespVO>> getModelPage(@Valid Ai1ModelPageReqVO pageReqVO) {
        PageResult<Ai1ModelDO> pageResult = modelService.getModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, Ai1ModelRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得模型精简列表", description = "只包含开启状态，用于 Agent 模型、知识库向量化模型的下拉选择")
    @Parameter(name = "providerId", description = "供应商编号", example = "1")
    @Parameter(name = "type", description = "类型", example = "0")
    public CommonResult<List<Ai1ModelRespVO>> getModelSimpleList(@RequestParam(value = "providerId", required = false) Long providerId,
                                                                 @RequestParam(value = "type", required = false) Integer type) {
        List<Ai1ModelDO> list = modelService.getModelListByProviderIdAndTypeAndStatus(
                providerId, type, CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, model -> new Ai1ModelRespVO()
                .setId(model.getId()).setProviderId(model.getProviderId())
                .setName(model.getName()).setModel(model.getModel()).setType(model.getType())));
    }

    @GetMapping("/remote-list")
    @Operation(summary = "拉取供应商远程可用模型", description = "并标注是否已导入")
    @Parameter(name = "providerId", description = "供应商编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('ai1:model:import')")
    public CommonResult<List<Ai1ModelRemoteRespVO>> getRemoteModelList(@RequestParam("providerId") Long providerId) {
        List<String> remoteModels = modelService.getRemoteModelList(providerId);
        List<Ai1ModelDO> existModelList = modelService.getModelListByProviderId(providerId);
        Set<String> existModels = convertSet(existModelList, Ai1ModelDO::getModel);
        return success(convertList(remoteModels, model -> new Ai1ModelRemoteRespVO(model, existModels.contains(model))));
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入远程模型", description = "返回实际导入数量")
    @PreAuthorize("@ss.hasPermission('ai1:model:import')")
    public CommonResult<Integer> importRemoteModelList(@Valid @RequestBody Ai1ModelImportReqVO importReqVO) {
        return success(modelService.importRemoteModelList(importReqVO));
    }

}
