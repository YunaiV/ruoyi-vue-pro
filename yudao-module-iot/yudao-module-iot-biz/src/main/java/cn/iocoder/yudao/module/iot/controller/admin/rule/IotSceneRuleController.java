package cn.iocoder.yudao.module.iot.controller.admin.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotSceneRuleUpdateStatusReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotSceneRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - IoT 场景联动")
@RestController
@RequestMapping("/iot/scene-rule")
@Validated
public class IotSceneRuleController {

    @Resource
    private IotSceneRuleService sceneRuleService;

    @PostMapping("/create")
    @Operation(summary = "创建场景联动")
    @PreAuthorize("@ss.hasPermission('iot:scene-rule:create')")
    public CommonResult<Long> createSceneRule(@Valid @RequestBody IotSceneRuleSaveReqVO createReqVO) {
        return success(sceneRuleService.createSceneRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新场景联动")
    @PreAuthorize("@ss.hasPermission('iot:scene-rule:update')")
    public CommonResult<Boolean> updateSceneRule(@Valid @RequestBody IotSceneRuleSaveReqVO updateReqVO) {
        sceneRuleService.updateSceneRule(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新场景联动状态")
    @PreAuthorize("@ss.hasPermission('iot:scene-rule:update')")
    public CommonResult<Boolean> updateSceneRuleStatus(@Valid @RequestBody IotSceneRuleUpdateStatusReqVO updateReqVO) {
        sceneRuleService.updateSceneRuleStatus(updateReqVO.getId(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除场景联动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:scene-rule:delete')")
    public CommonResult<Boolean> deleteSceneRule(@RequestParam("id") Long id) {
        sceneRuleService.deleteSceneRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得场景联动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:scene-rule:query')")
    public CommonResult<IotSceneRuleRespVO> getSceneRule(@RequestParam("id") Long id) {
        IotSceneRuleDO sceneRule = sceneRuleService.getSceneRule(id);
        return success(BeanUtils.toBean(sceneRule, IotSceneRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得场景联动分页")
    @PreAuthorize("@ss.hasPermission('iot:scene-rule:query')")
    public CommonResult<PageResult<IotSceneRuleRespVO>> getSceneRulePage(@Valid IotSceneRulePageReqVO pageReqVO) {
        PageResult<IotSceneRuleDO> pageResult = sceneRuleService.getSceneRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotSceneRuleRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取场景联动的精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<IotSceneRuleRespVO>> getSceneRuleSimpleList() {
        List<IotSceneRuleDO> list = sceneRuleService.getSceneRuleListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, scene -> // 只返回 id、name 字段
                new IotSceneRuleRespVO().setId(scene.getId()).setName(scene.getName())));
    }

}
