package cn.iocoder.yudao.module.iot.controller.admin.rule;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleScenePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene.IotRuleSceneSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotRuleSceneDO;
import cn.iocoder.yudao.module.iot.service.rule.IotRuleSceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

// TODO @芋艿：规则场景 要不要，统一改成 场景联动
@Tag(name = "管理后台 - IoT 规则场景")
@RestController
@RequestMapping("/iot/rule-scene")
@Validated
public class IotRuleSceneController {

    @Resource
    private IotRuleSceneService ruleSceneService;

    @PostMapping("/create")
    @Operation(summary = "创建规则场景（场景联动）")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:create')")
    public CommonResult<Long> createRuleScene(@Valid @RequestBody IotRuleSceneSaveReqVO createReqVO) {
        return success(ruleSceneService.createRuleScene(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新规则场景（场景联动）")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:update')")
    public CommonResult<Boolean> updateRuleScene(@Valid @RequestBody IotRuleSceneSaveReqVO updateReqVO) {
        ruleSceneService.updateRuleScene(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除规则场景（场景联动）")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:delete')")
    public CommonResult<Boolean> deleteRuleScene(@RequestParam("id") Long id) {
        ruleSceneService.deleteRuleScene(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得规则场景（场景联动）")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:query')")
    public CommonResult<IotRuleSceneRespVO> getRuleScene(@RequestParam("id") Long id) {
        IotRuleSceneDO ruleScene = ruleSceneService.getRuleScene(id);
        return success(BeanUtils.toBean(ruleScene, IotRuleSceneRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得规则场景（场景联动）分页")
    @PreAuthorize("@ss.hasPermission('iot:rule-scene:query')")
    public CommonResult<PageResult<IotRuleSceneRespVO>> getRuleScenePage(@Valid IotRuleScenePageReqVO pageReqVO) {
        PageResult<IotRuleSceneDO> pageResult = ruleSceneService.getRuleScenePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotRuleSceneRespVO.class));
    }

    @GetMapping("/test")
    @PermitAll
    public void test() {
        ruleSceneService.test();
    }

}
