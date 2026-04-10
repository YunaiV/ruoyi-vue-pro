package cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.itemreceipt.MesWmItemReceiptLineService;
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

@Tag(name = "管理后台 - MES 采购入库单行")
@RestController
@RequestMapping("/mes/wm/item-receipt-line")
@Validated
public class MesWmItemReceiptLineController {

    @Resource
    private MesWmItemReceiptLineService itemReceiptLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建采购入库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:create')")
    public CommonResult<Long> createItemReceiptLine(@Valid @RequestBody MesWmItemReceiptLineSaveReqVO createReqVO) {
        return success(itemReceiptLineService.createItemReceiptLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改采购入库单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:update')")
    public CommonResult<Boolean> updateItemReceiptLine(@Valid @RequestBody MesWmItemReceiptLineSaveReqVO updateReqVO) {
        itemReceiptLineService.updateItemReceiptLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购入库单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:delete')")
    public CommonResult<Boolean> deleteItemReceiptLine(@RequestParam("id") Long id) {
        itemReceiptLineService.deleteItemReceiptLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得采购入库单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:query')")
    public CommonResult<MesWmItemReceiptLineRespVO> getItemReceiptLine(@RequestParam("id") Long id) {
        MesWmItemReceiptLineDO line = itemReceiptLineService.getItemReceiptLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购入库单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-item-receipt:query')")
    public CommonResult<PageResult<MesWmItemReceiptLineRespVO>> getItemReceiptLinePage(
            @Valid MesWmItemReceiptLinePageReqVO pageReqVO) {
        PageResult<MesWmItemReceiptLineDO> pageResult = itemReceiptLineService.getItemReceiptLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmItemReceiptLineRespVO> buildRespVOList(List<MesWmItemReceiptLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmItemReceiptLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmItemReceiptLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode()).setItemName(item.getName()).setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
        });
    }

}
