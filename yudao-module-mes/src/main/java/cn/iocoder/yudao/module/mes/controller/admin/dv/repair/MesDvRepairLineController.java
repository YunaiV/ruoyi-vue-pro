package cn.iocoder.yudao.module.mes.controller.admin.dv.repair;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line.MesDvRepairLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line.MesDvRepairLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line.MesDvRepairLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.repair.MesDvRepairLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.service.dv.repair.MesDvRepairLineService;
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

@Tag(name = "管理后台 - MES 维修工单行")
@RestController
@RequestMapping("/mes/dv/repair-line")
@Validated
public class MesDvRepairLineController {

    @Resource
    private MesDvRepairLineService repairLineService;

    @Resource
    private MesDvSubjectService subjectService;

    @PostMapping("/create")
    @Operation(summary = "创建维修工单行")
    @PreAuthorize("@ss.hasPermission('mes:dv-repair:create')")
    public CommonResult<Long> createRepairLine(@Valid @RequestBody MesDvRepairLineSaveReqVO createReqVO) {
        return success(repairLineService.createRepairLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新维修工单行")
    @PreAuthorize("@ss.hasPermission('mes:dv-repair:update')")
    public CommonResult<Boolean> updateRepairLine(@Valid @RequestBody MesDvRepairLineSaveReqVO updateReqVO) {
        repairLineService.updateRepairLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除维修工单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-repair:delete')")
    public CommonResult<Boolean> deleteRepairLine(@RequestParam("id") Long id) {
        repairLineService.deleteRepairLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得维修工单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:dv-repair:query')")
    public CommonResult<MesDvRepairLineRespVO> getRepairLine(@RequestParam("id") Long id) {
        MesDvRepairLineDO repairLine = repairLineService.getRepairLine(id);
        if (repairLine == null) {
            return success(null);
        }
        return success(buildRepairLineRespVOList(Collections.singletonList(repairLine)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得维修工单行分页")
    @PreAuthorize("@ss.hasPermission('mes:dv-repair:query')")
    public CommonResult<PageResult<MesDvRepairLineRespVO>> getRepairLinePage(@Valid MesDvRepairLinePageReqVO pageReqVO) {
        PageResult<MesDvRepairLineDO> pageResult = repairLineService.getRepairLinePage(pageReqVO);
        return success(new PageResult<>(buildRepairLineRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出维修工单行 Excel")
    @PreAuthorize("@ss.hasPermission('mes:dv-repair:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRepairLineExcel(@Valid MesDvRepairLinePageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesDvRepairLineDO> list = repairLineService.getRepairLinePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "维修工单行.xls", "数据", MesDvRepairLineRespVO.class,
                buildRepairLineRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesDvRepairLineRespVO> buildRepairLineRespVOList(List<MesDvRepairLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesDvSubjectDO> subjectMap = subjectService.getSubjectMap(
                convertSet(list, MesDvRepairLineDO::getSubjectId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesDvRepairLineRespVO.class, vo ->
                MapUtils.findAndThen(subjectMap, vo.getSubjectId(), subject -> vo
                        .setSubjectName(subject.getName()).setSubjectContent(subject.getContent())
                        .setSubjectStandard(subject.getStandard())));
    }

}
