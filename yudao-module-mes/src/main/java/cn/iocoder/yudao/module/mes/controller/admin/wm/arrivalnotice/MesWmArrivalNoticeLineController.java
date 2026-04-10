package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 到货通知单行")
@RestController
@RequestMapping("/mes/wm/arrival-notice-line")
@Validated
public class MesWmArrivalNoticeLineController {

    @Resource
    private MesWmArrivalNoticeLineService arrivalNoticeLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @Resource
    private MesQcIqcService iqcService;

    @PostMapping("/create")
    @Operation(summary = "创建到货通知单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:create')")
    public CommonResult<Long> createArrivalNoticeLine(@Valid @RequestBody MesWmArrivalNoticeLineSaveReqVO createReqVO) {
        return success(arrivalNoticeLineService.createArrivalNoticeLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改到货通知单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:update')")
    public CommonResult<Boolean> updateArrivalNoticeLine(@Valid @RequestBody MesWmArrivalNoticeLineSaveReqVO updateReqVO) {
        arrivalNoticeLineService.updateArrivalNoticeLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除到货通知单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:delete')")
    public CommonResult<Boolean> deleteArrivalNoticeLine(@RequestParam("id") Long id) {
        arrivalNoticeLineService.deleteArrivalNoticeLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得到货通知单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:query')")
    public CommonResult<MesWmArrivalNoticeLineRespVO> getArrivalNoticeLine(@RequestParam("id") Long id) {
        MesWmArrivalNoticeLineDO line = arrivalNoticeLineService.getArrivalNoticeLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得到货通知单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:query')")
    public CommonResult<PageResult<MesWmArrivalNoticeLineRespVO>> getArrivalNoticeLinePage(
            @Valid MesWmArrivalNoticeLinePageReqVO pageReqVO) {
        PageResult<MesWmArrivalNoticeLineDO> pageResult = arrivalNoticeLineService.getArrivalNoticeLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/list-by-notice-id")
    @Operation(summary = "获得到货通知单行列表")
    @Parameter(name = "noticeId", description = "到货通知单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-arrival-notice:query')")
    public CommonResult<List<MesWmArrivalNoticeLineRespVO>> getArrivalNoticeLineListByNoticeId(
            @RequestParam("noticeId") Long noticeId) {
        List<MesWmArrivalNoticeLineDO> list = arrivalNoticeLineService.getArrivalNoticeLineListByNoticeId(noticeId);
        return success(buildRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmArrivalNoticeLineRespVO> buildRespVOList(List<MesWmArrivalNoticeLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmArrivalNoticeLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        Map<Long, MesQcIqcDO> iqcMap = iqcService.getIqcMap(
                convertSet(list, MesWmArrivalNoticeLineDO::getIqcId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmArrivalNoticeLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
            MapUtils.findAndThen(iqcMap, vo.getIqcId(), iqc -> vo.setIqcCode(iqc.getCode()));
        });
    }

}
