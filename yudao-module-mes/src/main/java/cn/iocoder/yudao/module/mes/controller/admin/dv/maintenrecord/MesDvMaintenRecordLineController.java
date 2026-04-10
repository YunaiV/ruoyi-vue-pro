package cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.service.dv.maintenrecord.MesDvMaintenRecordLineService;
import cn.iocoder.yudao.module.mes.service.dv.subject.MesDvSubjectService;
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

@Tag(name = "管理后台 - MES 设备保养记录明细")
@RestController
@RequestMapping("/mes/dv/mainten-record-line")
@Validated
public class MesDvMaintenRecordLineController {

    @Resource
    private MesDvMaintenRecordLineService maintenRecordLineService;

    @Resource
    private MesDvSubjectService subjectService;

    @PostMapping("/create")
    @Operation(summary = "创建设备保养记录明细")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:create')")
    public CommonResult<Long> createMaintenRecordLine(@Valid @RequestBody MesDvMaintenRecordLineSaveReqVO createReqVO) {
        return success(maintenRecordLineService.createMaintenRecordLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备保养记录明细")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:update')")
    public CommonResult<Boolean> updateMaintenRecordLine(@Valid @RequestBody MesDvMaintenRecordLineSaveReqVO updateReqVO) {
        maintenRecordLineService.updateMaintenRecordLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备保养记录明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:delete')")
    public CommonResult<Boolean> deleteMaintenRecordLine(@RequestParam("id") Long id) {
        maintenRecordLineService.deleteMaintenRecordLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备保养记录明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:query')")
    public CommonResult<MesDvMaintenRecordLineRespVO> getMaintenRecordLine(@RequestParam("id") Long id) {
        MesDvMaintenRecordLineDO maintenRecordLine = maintenRecordLineService.getMaintenRecordLine(id);
        if (maintenRecordLine == null) {
            return success(null);
        }
        return success(buildMaintenRecordLineRespVOList(Collections.singletonList(maintenRecordLine)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备保养记录明细分页")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:query')")
    public CommonResult<PageResult<MesDvMaintenRecordLineRespVO>> getMaintenRecordLinePage(@Valid MesDvMaintenRecordLinePageReqVO pageReqVO) {
        PageResult<MesDvMaintenRecordLineDO> pageResult = maintenRecordLineService.getMaintenRecordLinePage(pageReqVO);
        return success(new PageResult<>(buildMaintenRecordLineRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备保养记录明细 Excel")
    @PreAuthorize("@ss.hasPermission('mes:dv-mainten-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMaintenRecordLineExcel(@Valid MesDvMaintenRecordLinePageReqVO pageReqVO,
                                              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesDvMaintenRecordLineDO> list = maintenRecordLineService.getMaintenRecordLinePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "设备保养记录明细.xls", "数据", MesDvMaintenRecordLineRespVO.class,
                buildMaintenRecordLineRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesDvMaintenRecordLineRespVO> buildMaintenRecordLineRespVOList(List<MesDvMaintenRecordLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesDvSubjectDO> subjectMap = subjectService.getSubjectMap(
                convertSet(list, MesDvMaintenRecordLineDO::getSubjectId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesDvMaintenRecordLineRespVO.class, vo ->
                MapUtils.findAndThen(subjectMap, vo.getSubjectId(), subject -> vo
                        .setSubjectName(subject.getName()).setSubjectContent(subject.getContent())
                        .setSubjectStandard(subject.getStandard())));
    }

}
