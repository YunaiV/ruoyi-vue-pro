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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT OTA 升级任务记录")
@RestController
@RequestMapping("/iot/ota/task-record")
@Validated
public class IotOtaTaskRecordController {

    @Resource
    private IotOtaTaskRecordService otaTaskRecordService;

    @GetMapping("/get-statistics")
    @Operation(summary = "固件升级设备统计")
    @PreAuthorize("@ss.hasPermission('iot:ota-task-record:query')")
    @Parameter(name = "firmwareId", description = "固件编号", required = true, example = "1024")
    public CommonResult<Map<Integer, Long>> getOtaTaskRecordStatistics(@RequestParam(value = "firmwareId") Long firmwareId) {
        return success(otaTaskRecordService.getOtaTaskRecordStatistics(firmwareId));
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得升级记录分页 tab 数量")
    @PreAuthorize("@ss.hasPermission('iot:ota-task-record:query')")
    public CommonResult<Map<Integer, Long>> getOtaTaskRecordCount(@Valid IotOtaTaskRecordPageReqVO pageReqVO) {
        return success(otaTaskRecordService.getOtaTaskRecordCount(pageReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得升级记录分页")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:query')")
    public CommonResult<PageResult<IotOtaTaskRecordRespVO>> getOtaTaskRecordPage(
            @Valid IotOtaTaskRecordPageReqVO pageReqVO) {
        PageResult<IotOtaTaskRecordDO> pageResult = otaTaskRecordService.getOtaTaskRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOtaTaskRecordRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得升级记录")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:query')")
    @Parameter(name = "id", description = "升级记录编号", required = true, example = "1024")
    public CommonResult<IotOtaTaskRecordRespVO> getOtaTaskRecord(@RequestParam("id") Long id) {
        IotOtaTaskRecordDO upgradeRecord = otaTaskRecordService.getOtaTaskRecord(id);
        return success(BeanUtils.toBean(upgradeRecord, IotOtaTaskRecordRespVO.class));
    }

}
