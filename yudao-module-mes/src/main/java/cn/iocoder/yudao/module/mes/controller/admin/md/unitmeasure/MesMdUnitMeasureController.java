package cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasurePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasureRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.unitmeasure.vo.MesMdUnitMeasureSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - MES 计量单位")
@RestController
@RequestMapping("/mes/md/unit-measure")
@Validated
public class MesMdUnitMeasureController {

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建计量单位")
    @PreAuthorize("@ss.hasPermission('mes:md-unit-measure:create')")
    public CommonResult<Long> createUnitMeasure(@Valid @RequestBody MesMdUnitMeasureSaveReqVO createReqVO) {
        return success(unitMeasureService.createUnitMeasure(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新计量单位")
    @PreAuthorize("@ss.hasPermission('mes:md-unit-measure:update')")
    public CommonResult<Boolean> updateUnitMeasure(@Valid @RequestBody MesMdUnitMeasureSaveReqVO updateReqVO) {
        unitMeasureService.updateUnitMeasure(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除计量单位")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-unit-measure:delete')")
    public CommonResult<Boolean> deleteUnitMeasure(@RequestParam("id") Long id) {
        unitMeasureService.deleteUnitMeasure(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得计量单位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-unit-measure:query')")
    public CommonResult<MesMdUnitMeasureRespVO> getUnitMeasure(@RequestParam("id") Long id) {
        MesMdUnitMeasureDO unitMeasure = unitMeasureService.getUnitMeasure(id);
        return success(BeanUtils.toBean(unitMeasure, MesMdUnitMeasureRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得计量单位分页")
    @PreAuthorize("@ss.hasPermission('mes:md-unit-measure:query')")
    public CommonResult<PageResult<MesMdUnitMeasureRespVO>> getUnitMeasurePage(@Valid MesMdUnitMeasurePageReqVO pageReqVO) {
        PageResult<MesMdUnitMeasureDO> pageResult = unitMeasureService.getUnitMeasurePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesMdUnitMeasureRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得计量单位精简列表", description = "只包含被开启的单位，主要用于前端的下拉选项")
    public CommonResult<List<MesMdUnitMeasureRespVO>> getUnitMeasureSimpleList() {
        List<MesMdUnitMeasureDO> list = unitMeasureService.getUnitMeasureListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, unit -> new MesMdUnitMeasureRespVO()
                .setId(unit.getId()).setCode(unit.getCode()).setName(unit.getName())
                .setPrimaryFlag(unit.getPrimaryFlag()).setPrimaryId(unit.getPrimaryId())
                .setChangeRate(unit.getChangeRate()).setRemark(unit.getRemark())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出计量单位 Excel")
    @PreAuthorize("@ss.hasPermission('mes:md-unit-measure:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUnitMeasureExcel(@Valid MesMdUnitMeasurePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesMdUnitMeasureDO> list = unitMeasureService.getUnitMeasurePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "计量单位.xls", "数据", MesMdUnitMeasureRespVO.class,
                        BeanUtils.toBean(list, MesMdUnitMeasureRespVO.class));
    }

}
