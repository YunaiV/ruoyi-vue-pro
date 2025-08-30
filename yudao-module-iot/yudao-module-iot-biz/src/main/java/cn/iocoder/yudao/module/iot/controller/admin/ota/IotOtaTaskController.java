package cn.iocoder.yudao.module.iot.controller.admin.ota;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.IotOtaTaskRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskDO;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskService;
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
@RequestMapping("/iot/ota/task")
@Validated
public class IotOtaTaskController {

    @Resource
    private IotOtaTaskService otaTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建 OTA 升级任务")
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-task:create')")
    public CommonResult<Long> createOtaTask(@Valid @RequestBody IotOtaTaskCreateReqVO createReqVO) {
        return success(otaTaskService.createOtaTask(createReqVO));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消 OTA 升级任务")
    @Parameter(name = "id", description = "升级任务编号", required = true)
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-task:cancel')")
    public CommonResult<Boolean> cancelOtaTask(@RequestParam("id") Long id) {
        otaTaskService.cancelOtaTask(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OTA 升级任务分页")
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-task:query')")
    public CommonResult<PageResult<IotOtaTaskRespVO>> getOtaTaskPage(@Valid IotOtaTaskPageReqVO pageReqVO) {
        PageResult<IotOtaTaskDO> pageResult = otaTaskService.getOtaTaskPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOtaTaskRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OTA 升级任务")
    @Parameter(name = "id", description = "升级任务编号", required = true, example = "1024")
    @PreAuthorize(value = "@ss.hasPermission('iot:ota-task:query')")
    public CommonResult<IotOtaTaskRespVO> getOtaTask(@RequestParam("id") Long id) {
        IotOtaTaskDO upgradeTask = otaTaskService.getOtaTask(id);
        return success(BeanUtils.toBean(upgradeTask, IotOtaTaskRespVO.class));
    }

}
