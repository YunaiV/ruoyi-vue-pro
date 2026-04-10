package cn.iocoder.yudao.module.mes.controller.admin.qc.rqc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.line.MesQcRqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.line.MesQcRqcLineRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.qc.rqc.MesQcRqcLineService;
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

@Tag(name = "管理后台 - MES 退货检验单行")
@RestController
@RequestMapping("/mes/qc/rqc/line")
@Validated
public class MesQcRqcLineController {

    @Resource
    private MesQcRqcLineService rqcLineService;
    @Resource
    private MesQcIndicatorService indicatorService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @GetMapping("/get")
    @Operation(summary = "获得退货检验单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:qc-rqc:query')")
    public CommonResult<MesQcRqcLineRespVO> getRqcLine(@RequestParam("id") Long id) {
        MesQcRqcLineDO line = rqcLineService.getRqcLine(id);
        return success(buildLineRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得退货检验单行分页")
    @PreAuthorize("@ss.hasPermission('mes:qc-rqc:query')")
    public CommonResult<PageResult<MesQcRqcLineRespVO>> getRqcLinePage(@Valid MesQcRqcLinePageReqVO pageReqVO) {
        PageResult<MesQcRqcLineDO> pageResult = rqcLineService.getRqcLinePage(pageReqVO);
        return success(new PageResult<>(buildLineRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesQcRqcLineRespVO> buildLineRespVOList(List<MesQcRqcLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询检测指标
        Map<Long, MesQcIndicatorDO> indicatorMap = indicatorService.getIndicatorMap(
                convertSet(list, MesQcRqcLineDO::getIndicatorId));
        // 批量查询计量单位
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(list, MesQcRqcLineDO::getUnitMeasureId));
        // 拼装 VO
        return BeanUtils.toBean(list, MesQcRqcLineRespVO.class, vo -> {
            findAndThen(indicatorMap, vo.getIndicatorId(), indicator ->
                    vo.setIndicatorCode(indicator.getCode()).setIndicatorName(indicator.getName())
                            .setIndicatorType(indicator.getType()));
            findAndThen(unitMeasureMap, vo.getUnitMeasureId(),
                    unit -> vo.setUnitMeasureName(unit.getName()));
        });
    }

}
