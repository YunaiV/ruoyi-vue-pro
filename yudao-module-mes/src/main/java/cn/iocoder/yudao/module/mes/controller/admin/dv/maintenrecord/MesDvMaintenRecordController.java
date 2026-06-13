package cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordDO;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanService;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import cn.iocoder.yudao.module.mes.service.dv.maintenrecord.MesDvMaintenRecordService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 设备保养记录")
@RestController
@RequestMapping("/mes/dv/mainten-record")
@Validated
public class MesDvMaintenRecordController {

    @Resource
    private MesDvMaintenRecordService maintenRecordService;
    @Resource
    private MesDvCheckPlanService checkPlanService;
    @Resource
    private MesDvMachineryService machineryService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建设备保养记录")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:create')")
    public CommonResult<Long> createMaintenRecord(@Valid @RequestBody MesDvMaintenRecordSaveReqVO createReqVO) {
        return success(maintenRecordService.createMaintenRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备保养记录")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:update')")
    public CommonResult<Boolean> updateMaintenRecord(@Valid @RequestBody MesDvMaintenRecordSaveReqVO updateReqVO) {
        maintenRecordService.updateMaintenRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备保养记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:delete')")
    public CommonResult<Boolean> deleteMaintenRecord(@RequestParam("id") Long id) {
        maintenRecordService.deleteMaintenRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备保养记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:query')")
    public CommonResult<MesDvMaintenRecordRespVO> getMaintenRecord(@RequestParam("id") Long id) {
        MesDvMaintenRecordDO maintenRecord = maintenRecordService.getMaintenRecord(id);
        if (maintenRecord == null) {
            return success(null);
        }
        return success(buildMaintenRecordRespVOList(Collections.singletonList(maintenRecord)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备保养记录分页")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:query')")
    public CommonResult<PageResult<MesDvMaintenRecordRespVO>> getMaintenRecordPage(@Valid MesDvMaintenRecordPageReqVO pageReqVO) {
        PageResult<MesDvMaintenRecordDO> pageResult = maintenRecordService.getMaintenRecordPage(pageReqVO);
        return success(new PageResult<>(buildMaintenRecordRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备保养记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMaintenRecordExcel(@Valid MesDvMaintenRecordPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesDvMaintenRecordDO> list = maintenRecordService.getMaintenRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "设备保养记录.xls", "数据", MesDvMaintenRecordRespVO.class,
                buildMaintenRecordRespVOList(list));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交设备保养记录（草稿→已提交）")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:update')")
    public CommonResult<Boolean> submitMaintenRecord(@RequestParam("id") Long id) {
        maintenRecordService.submitMaintenRecord(id);
        return success(true);
    }

    // ==================== 拼接 VO ====================

    private List<MesDvMaintenRecordRespVO> buildMaintenRecordRespVOList(List<MesDvMaintenRecordDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesDvCheckPlanDO> planMap = checkPlanService.getCheckPlanMap(
                convertSet(list, MesDvMaintenRecordDO::getPlanId));
        Map<Long, MesDvMachineryDO> machineryMap = machineryService.getMachineryMap(
                convertSet(list, MesDvMaintenRecordDO::getMachineryId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesDvMaintenRecordDO::getUserId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesDvMaintenRecordRespVO.class, vo -> {
            MapUtils.findAndThen(planMap, vo.getPlanId(),
                    plan -> vo.setPlanName(plan.getName())
                            .setPlanCode(plan.getCode())
                            .setPlanStartDate(plan.getStartDate())
                            .setPlanEndDate(plan.getEndDate())
                            .setPlanCycleType(plan.getCycleType())
                            .setPlanCycleCount(plan.getCycleCount()));
            MapUtils.findAndThen(machineryMap, vo.getMachineryId(), machinery -> vo
                    .setMachineryCode(machinery.getCode()).setMachineryName(machinery.getName())
                    .setMachineryBrand(machinery.getBrand()).setMachinerySpecification(machinery.getSpecification()));
            MapUtils.findAndThen(userMap, vo.getUserId(),
                    user -> vo.setNickname(user.getNickname()));
        });
    }

}
