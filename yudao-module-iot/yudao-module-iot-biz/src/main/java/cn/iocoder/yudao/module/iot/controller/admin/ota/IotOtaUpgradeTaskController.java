package cn.iocoder.yudao.module.iot.controller.admin.ota;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.task.IotOtaUpgradeTaskSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaUpgradeTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT OTA 升级任务")
@RestController
@RequestMapping("/iot/ota-upgrade-task")
@Validated
public class IotOtaUpgradeTaskController {

    @Resource
    private IotOtaUpgradeTaskService upgradeTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建升级任务")
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-upgrade-task:create')")
    public CommonResult<Long> createUpgradeTask(@Valid @RequestBody IotOtaUpgradeTaskSaveReqVO createReqVO) {
        return success(upgradeTaskService.createUpgradeTask(createReqVO));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消升级任务")
    @Parameter(name = "id", description = "升级任务编号", required = true)
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-upgrade-task:cancel')")
    public CommonResult<Boolean> cancelUpgradeTask(@RequestParam("id") Long id) {
        upgradeTaskService.cancelUpgradeTask(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得升级任务分页")
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-upgrade-task:query')")
    public CommonResult<PageResult<IotOtaUpgradeTaskRespVO>> getUpgradeTaskPage(@Valid IotOtaUpgradeTaskPageReqVO pageReqVO) {
        PageResult<IotOtaUpgradeTaskDO> pageResult = upgradeTaskService.getUpgradeTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOtaUpgradeTaskRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得升级任务")
    @Parameter(name = "id", description = "升级任务编号", required = true, example = "1024")
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-upgrade-task:query')")
    public CommonResult<IotOtaUpgradeTaskRespVO> getUpgradeTask(@RequestParam("id") Long id) {
        IotOtaUpgradeTaskDO upgradeTask = upgradeTaskService.getUpgradeTask(id);
        return success(BeanUtils.toBean(upgradeTask, IotOtaUpgradeTaskRespVO.class));
    }

}
