package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line.MesWmSalesNoticeLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line.MesWmSalesNoticeLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line.MesWmSalesNoticeLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.salesnotice.MesWmSalesNoticeLineService;
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

@Tag(name = "管理后台 - MES 发货通知单行")
@RestController
@RequestMapping("/mes/wm/sales-notice-line")
@Validated
public class MesWmSalesNoticeLineController {

    @Resource
    private MesWmSalesNoticeLineService salesNoticeLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建发货通知单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:create')")
    public CommonResult<Long> createSalesNoticeLine(@Valid @RequestBody MesWmSalesNoticeLineSaveReqVO createReqVO) {
        return success(salesNoticeLineService.createSalesNoticeLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改发货通知单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:update')")
    public CommonResult<Boolean> updateSalesNoticeLine(@Valid @RequestBody MesWmSalesNoticeLineSaveReqVO updateReqVO) {
        salesNoticeLineService.updateSalesNoticeLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除发货通知单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:delete')")
    public CommonResult<Boolean> deleteSalesNoticeLine(@RequestParam("id") Long id) {
        salesNoticeLineService.deleteSalesNoticeLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得发货通知单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:query')")
    public CommonResult<MesWmSalesNoticeLineRespVO> getSalesNoticeLine(@RequestParam("id") Long id) {
        MesWmSalesNoticeLineDO line = salesNoticeLineService.getSalesNoticeLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得发货通知单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-sales-notice:query')")
    public CommonResult<PageResult<MesWmSalesNoticeLineRespVO>> getSalesNoticeLinePage(
            @Valid MesWmSalesNoticeLinePageReqVO pageReqVO) {
        PageResult<MesWmSalesNoticeLineDO> pageResult = salesNoticeLineService.getSalesNoticeLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmSalesNoticeLineRespVO> buildRespVOList(List<MesWmSalesNoticeLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmSalesNoticeLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmSalesNoticeLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
        });
    }

}
