package cn.iocoder.yudao.module.iot.controller.admin.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneUpdateStatusReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotRuleSceneService;
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
@RequestMapping("/iot/rule-scene")
@Validated
public class IotRuleSceneController {

    @Resource
    private IotRuleSceneService ruleSceneService;

    @PostMapping("/create")
    @Operation(summary = "创建场景联动")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:create')")
    public CommonResult<Long> createRuleScene(@Valid @RequestBody IotRuleSceneSaveReqVO createReqVO) {
        return success(ruleSceneService.createRuleScene(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新场景联动")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:update')")
    public CommonResult<Boolean> updateRuleScene(@Valid @RequestBody IotRuleSceneSaveReqVO updateReqVO) {
        ruleSceneService.updateRuleScene(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新场景联动状态")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:update')")
    public CommonResult<Boolean> updateRuleSceneStatus(@Valid @RequestBody IotRuleSceneUpdateStatusReqVO updateReqVO) {
        ruleSceneService.updateRuleSceneStatus(updateReqVO.getId(), updateReqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除场景联动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:delete')")
    public CommonResult<Boolean> deleteRuleScene(@RequestParam("id") Long id) {
        ruleSceneService.deleteRuleScene(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得场景联动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:query')")
    public CommonResult<IotRuleSceneRespVO> getRuleScene(@RequestParam("id") Long id) {
        IotSceneRuleDO ruleScene = ruleSceneService.getRuleScene(id);
        return success(BeanUtils.toBean(ruleScene, IotRuleSceneRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得场景联动分页")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:query')")
    public CommonResult<PageResult<IotRuleSceneRespVO>> getRuleScenePage(@Valid IotRuleScenePageReqVO pageReqVO) {
        PageResult<IotSceneRuleDO> pageResult = ruleSceneService.getRuleScenePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotRuleSceneRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取场景联动的精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<IotRuleSceneRespVO>> getRuleSceneSimpleList() {
        List<IotSceneRuleDO> list = ruleSceneService.getRuleSceneListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, scene -> // 只返回 id、name 字段
                new IotRuleSceneRespVO().setId(scene.getId()).setName(scene.getName())));
    }

}
