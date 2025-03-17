package cn.iocoder.yudao.module.iot.controller.admin.ota;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.upgrade.record.IotOtaUpgradeRecordRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeRecordDO;
import cn.iocoder.yudao.module.iot.service.ota.IotOtaUpgradeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT OTA 升级记录")
@RestController
@RequestMapping("/iot/ota-upgrade-record")
@Validated
public class IotOtaUpgradeRecordController {

    @Resource
    private IotOtaUpgradeRecordService upgradeRecordService;

    @GetMapping("/get-statistics")
    @Operation(summary = "固件升级设备统计")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:query')")
    @Parameter(name = "firmwareId", description = "固件编号", required = true, example = "1024")
    public CommonResult<Map<Integer, Long>> getOtaUpgradeRecordStatistics(@RequestParam(value = "firmwareId") Long firmwareId) {
        return success(upgradeRecordService.getOtaUpgradeRecordStatistics(firmwareId));
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得升级记录分页 tab 数量")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:query')")
    public CommonResult<Map<Integer, Long>> getOtaUpgradeRecordCount(
            @Valid IotOtaUpgradeRecordPageReqVO pageReqVO) {
        return success(upgradeRecordService.getOtaUpgradeRecordCount(pageReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得升级记录分页")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:query')")
    public CommonResult<PageResult<IotOtaUpgradeRecordRespVO>> getUpgradeRecordPage(
            @Valid IotOtaUpgradeRecordPageReqVO pageReqVO) {
        PageResult<IotOtaUpgradeRecordDO> pageResult = upgradeRecordService.getUpgradeRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOtaUpgradeRecordRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得升级记录")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:query')")
    @Parameter(name = "id", description = "升级记录编号", required = true, example = "1024")
    public CommonResult<IotOtaUpgradeRecordRespVO> getUpgradeRecord(@RequestParam("id") Long id) {
        IotOtaUpgradeRecordDO upgradeRecord = upgradeRecordService.getUpgradeRecord(id);
        return success(BeanUtils.toBean(upgradeRecord, IotOtaUpgradeRecordRespVO.class));
    }

    @PutMapping("/retry")
    @Operation(summary = "重试升级记录")
    @PreAuthorize("@ss.hasPermission('iot:ota-upgrade-record:retry')")
    @Parameter(name = "id", description = "升级记录编号", required = true, example = "1024")
    public CommonResult<Boolean> retryUpgradeRecord(@RequestParam("id") Long id) {
        upgradeRecordService.retryUpgradeRecord(id);
        return success(true);
    }

}
