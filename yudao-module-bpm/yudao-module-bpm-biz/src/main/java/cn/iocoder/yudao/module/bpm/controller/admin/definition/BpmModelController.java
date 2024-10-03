package cn.iocoder.yudao.module.bpm.controller.admin.definition;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.*;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.convert.definition.BpmModelConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.service.definition.BpmCategoryService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmFormService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 流程模型")
@RestController
@RequestMapping("/bpm/model")
@Validated
public class BpmModelController {

    @Resource
    private BpmModelService modelService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmCategoryService categoryService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得模型分页")
    public CommonResult<PageResult<BpmModelRespVO>> getModelPage(BpmModelPageReqVO pageVO) {
        PageResult<Model> pageResult = modelService.getModelPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        // 获得 Form 表单
        Set<Long> formIds = convertSet(pageResult.getList(), model -> {
            BpmModelMetaInfoVO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoVO.class);
            return metaInfo != null ? metaInfo.getFormId() : null;
        });
        Map<Long, BpmFormDO> formMap = formService.getFormMap(formIds);
        // 获得 Category Map
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(pageResult.getList(), Model::getCategory));
        // 获得 Deployment Map
        Set<String> deploymentIds = new HashSet<>();
        pageResult.getList().forEach(model -> CollectionUtils.addIfNotNull(deploymentIds, model.getDeploymentId()));
        Map<String, Deployment> deploymentMap = processDefinitionService.getDeploymentMap(deploymentIds);
        // 获得 ProcessDefinition Map
        List<ProcessDefinition> processDefinitions = processDefinitionService.getProcessDefinitionListByDeploymentIds(deploymentIds);
        Map<String, ProcessDefinition> processDefinitionMap = convertMap(processDefinitions, ProcessDefinition::getDeploymentId);
        // 获得 User Map
        Set<Long> userIds = CollectionUtils.convertSetByFlatMap(pageResult.getList(), model -> {
            BpmModelMetaInfoVO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoVO.class);
            return metaInfo != null ? metaInfo.getStartUserIds().stream() : Stream.empty();
        });
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        return success(BpmModelConvert.INSTANCE.buildModelPage(pageResult,
                formMap, categoryMap, deploymentMap, processDefinitionMap, userMap));
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:query')")
    public CommonResult<BpmModelRespVO> getModel(@RequestParam("id") String id) {
        Model model = modelService.getModel(id);
        if (model == null) {
            return null;
        }
        byte[] bpmnBytes = modelService.getModelBpmnXML(id);
        return success(BpmModelConvert.INSTANCE.buildModel(model, bpmnBytes));
    }

    @PostMapping("/create")
    @Operation(summary = "新建模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:create')")
    public CommonResult<String> createModel(@Valid @RequestBody BpmModelSaveReqVO createRetVO) {
        return success(modelService.createModel(createRetVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody BpmModelSaveReqVO modelVO) {
        modelService.updateModel(modelVO);
        return success(true);
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:deploy')")
    public CommonResult<Boolean> deployModel(@RequestParam("id") String id) {
        modelService.deployModel(id);
        return success(true);
    }

    @PutMapping("/update-state")
    @Operation(summary = "修改模型的状态", description = "实际更新的部署的流程定义的状态")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModelState(@Valid @RequestBody BpmModelUpdateStateReqVO reqVO) {
        modelService.updateModelState(reqVO.getId(), reqVO.getState());
        return success(true);
    }

    @PutMapping("/update-bpmn")
    @Operation(summary = "修改模型的 BPMN")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModelBpmn(@Valid @RequestBody BpmModeUpdateBpmnReqVO reqVO) {
        modelService.updateModelBpmnXml(reqVO.getId(), reqVO.getBpmnXml());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") String id) {
        modelService.deleteModel(id);
        return success(true);
    }

    // ========== 仿钉钉/飞书的精简模型 =========

    @GetMapping("/simple/get")
    @Operation(summary = "获得仿钉钉流程设计模型")
    @Parameter(name = "modelId", description = "流程模型编号", required = true, example = "a2c5eee0-eb6c-11ee-abf4-0c37967c420a")
    public CommonResult<BpmSimpleModelNodeVO> getSimpleModel(@RequestParam("id") String modelId){
        return success(modelService.getSimpleModel(modelId));
    }

    @PostMapping("/simple/update")
    @Operation(summary = "保存仿钉钉流程设计模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateSimpleModel(@Valid @RequestBody BpmSimpleModelUpdateReqVO reqVO) {
        modelService.updateSimpleModel(reqVO);
        return success(Boolean.TRUE);
    }

}
