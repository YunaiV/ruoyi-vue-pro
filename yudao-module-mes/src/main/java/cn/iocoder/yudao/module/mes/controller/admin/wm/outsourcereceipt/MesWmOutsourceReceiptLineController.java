package cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line.MesWmOutsourceReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line.MesWmOutsourceReceiptLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line.MesWmOutsourceReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt.MesWmOutsourceReceiptLineService;
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

@Tag(name = "管理后台 - MES 外协入库单行")
@RestController
@RequestMapping("/mes/wm/outsource-receipt-line")
@Validated
public class MesWmOutsourceReceiptLineController {

    @Resource
    private MesWmOutsourceReceiptLineService receiptLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建外协入库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:create')")
    public CommonResult<Long> createOutsourceReceiptLine(@Valid @RequestBody MesWmOutsourceReceiptLineSaveReqVO createReqVO) {
        return success(receiptLineService.createOutsourceReceiptLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改外协入库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:update')")
    public CommonResult<Boolean> updateOutsourceReceiptLine(@Valid @RequestBody MesWmOutsourceReceiptLineSaveReqVO updateReqVO) {
        receiptLineService.updateOutsourceReceiptLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除外协入库单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:delete')")
    public CommonResult<Boolean> deleteOutsourceReceiptLine(@RequestParam("id") Long id) {
        receiptLineService.deleteOutsourceReceiptLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得外协入库单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:query')")
    public CommonResult<MesWmOutsourceReceiptLineRespVO> getOutsourceReceiptLine(@RequestParam("id") Long id) {
        MesWmOutsourceReceiptLineDO line = receiptLineService.getOutsourceReceiptLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得外协入库单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-outsource-receipt:query')")
    public CommonResult<PageResult<MesWmOutsourceReceiptLineRespVO>> getOutsourceReceiptLinePage(
            @Valid MesWmOutsourceReceiptLinePageReqVO pageReqVO) {
        PageResult<MesWmOutsourceReceiptLineDO> pageResult = receiptLineService.getOutsourceReceiptLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmOutsourceReceiptLineRespVO> buildRespVOList(List<MesWmOutsourceReceiptLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmOutsourceReceiptLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmOutsourceReceiptLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
        });
    }

}
