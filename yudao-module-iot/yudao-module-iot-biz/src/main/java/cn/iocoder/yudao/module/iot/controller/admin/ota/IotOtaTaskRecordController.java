package cn.iocoder.yudao.module.iot.controller.admin.ota;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.task.record.IotOtaTaskRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaTaskRecordDO;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaTaskRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT OTA 升级任务记录")
@RestController
@RequestMapping("/iot/ota/task/record")
@Validated
public class IotOtaTaskRecordController {

    @Resource
    private IotOtaTaskRecordService otaTaskRecordService;

    @GetMapping("/get-status-count")
    @Operation(summary = "获得 OTA 升级记录状态统计")
    @Parameters({
        @Parameter(name = "firmwareId", description = "固件编号", example = "1024"),
        @Parameter(name = "taskId", description = "升级任务编号", example = "2048")
    })
    @PreAuthorize("@ss.hasPermission('iot:ota-task-record:query')")
    public CommonResult<Map<Integer, Long>> getOtaTaskRecordStatusCountMap(
            @RequestParam(value = "firmwareId", required = false) Long firmwareId,
            @RequestParam(value = "taskId", required = false) Long taskId) {
        return success(otaTaskRecordService.getOtaTaskRecordStatusCountMap(firmwareId, taskId));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OTA 升级记录分页")
    @PreAuthorize("@ss.hasPermission('iot:ota-task-record:query')")
    public CommonResult<PageResult<IotOtaTaskRecordRespVO>> getOtaTaskRecordPage(
            @Valid IotOtaTaskRecordPageReqVO pageReqVO) {
        PageResult<IotOtaTaskRecordDO> pageResult = otaTaskRecordService.getOtaTaskRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOtaTaskRecordRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OTA 升级记录")
    @PreAuthorize("@ss.hasPermission('iot:ota-task-record:query')")
    @Parameter(name = "id", description = "升级记录编号", required = true, example = "1024")
    public CommonResult<IotOtaTaskRecordRespVO> getOtaTaskRecord(@RequestParam("id") Long id) {
        IotOtaTaskRecordDO upgradeRecord = otaTaskRecordService.getOtaTaskRecord(id);
        return success(BeanUtils.toBean(upgradeRecord, IotOtaTaskRecordRespVO.class));
    }

}
