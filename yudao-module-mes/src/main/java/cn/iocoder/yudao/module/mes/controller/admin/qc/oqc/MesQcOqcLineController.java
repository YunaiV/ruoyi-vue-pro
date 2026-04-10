package cn.iocoder.yudao.module.mes.controller.admin.qc.oqc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.line.MesQcOqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.line.MesQcOqcLineRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.qc.oqc.MesQcOqcLineService;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - MES 出货检验单行")
@RestController
@RequestMapping("/mes/qc/oqc/line")
@Validated
public class MesQcOqcLineController {

    @Resource
    private MesQcOqcLineService oqcLineService;
    @Resource
    private MesQcIndicatorService indicatorService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @GetMapping("/get")
    @Operation(summary = "获得出货检验单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:query')")
    public CommonResult<MesQcOqcLineRespVO> getOqcLine(@RequestParam("id") Long id) {
        MesQcOqcLineDO line = oqcLineService.getOqcLine(id);
        return success(buildLineRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出货检验单行分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-oqc:query')")
    public CommonResult<PageResult<MesQcOqcLineRespVO>> getOqcLinePage(@Valid MesQcOqcLinePageReqVO pageReqVO) {
        PageResult<MesQcOqcLineDO> pageResult = oqcLineService.getOqcLinePage(pageReqVO);
        return success(new PageResult<>(buildLineRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesQcOqcLineRespVO> buildLineRespVOList(List<MesQcOqcLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询检测指标
        Map<Long, MesQcIndicatorDO> indicatorMap = indicatorService.getIndicatorMap(
                convertSet(list, MesQcOqcLineDO::getIndicatorId));
        // 批量查询计量单位
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(list, MesQcOqcLineDO::getUnitMeasureId));
        // 拼装 VO
        return BeanUtils.toBean(list, MesQcOqcLineRespVO.class, vo -> {
            findAndThen(indicatorMap, vo.getIndicatorId(), indicator ->
                    vo.setIndicatorCode(indicator.getCode()).setIndicatorName(indicator.getName())
                            .setIndicatorType(indicator.getType()));
            findAndThen(unitMeasureMap, vo.getUnitMeasureId(),
                    unit -> vo.setUnitMeasureName(unit.getName()));
        });
    }

}
