package cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.returnvendor.MesWmReturnVendorLineService;
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

@Tag(name = "管理后台 - MES 供应商退货单行")
@RestController
@RequestMapping("/mes/wm/return-vendor-line")
@Validated
public class MesWmReturnVendorLineController {

    @Resource
    private MesWmReturnVendorLineService returnVendorLineService;

    @Resource
    private MesMdItemService itemService;

    @Resource
    private MesMdUnitMeasureService unitMeasureService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商退货单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:create')")
    public CommonResult<Long> createReturnVendorLine(@Valid @RequestBody MesWmReturnVendorLineSaveReqVO createReqVO) {
        return success(returnVendorLineService.createReturnVendorLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改供应商退货单行")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:update')")
    public CommonResult<Boolean> updateReturnVendorLine(@Valid @RequestBody MesWmReturnVendorLineSaveReqVO updateReqVO) {
        returnVendorLineService.updateReturnVendorLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商退货单行")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:delete')")
    public CommonResult<Boolean> deleteReturnVendorLine(@RequestParam("id") Long id) {
        returnVendorLineService.deleteReturnVendorLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商退货单行")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:query')")
    public CommonResult<MesWmReturnVendorLineRespVO> getReturnVendorLine(@RequestParam("id") Long id) {
        MesWmReturnVendorLineDO line = returnVendorLineService.getReturnVendorLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商退货单行分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:query')")
    public CommonResult<PageResult<MesWmReturnVendorLineRespVO>> getReturnVendorLinePage(
            @Valid MesWmReturnVendorLinePageReqVO pageReqVO) {
        PageResult<MesWmReturnVendorLineDO> pageResult = returnVendorLineService.getReturnVendorLinePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/list-by-return-vendor")
    @Operation(summary = "获得供应商退货单行列表（按退货单编号）")
    @Parameter(name = "returnId", description = "退货单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-vendor:query')")
    public CommonResult<List<MesWmReturnVendorLineRespVO>> getReturnVendorLineListByReturnId(
            @RequestParam("returnId") Long returnId) {
        List<MesWmReturnVendorLineDO> list = returnVendorLineService.getReturnVendorLineListByReturnId(returnId);
        return success(buildRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmReturnVendorLineRespVO> buildRespVOList(List<MesWmReturnVendorLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdItemDO> itemMap = itemService.getItemMap(
                convertSet(list, MesWmReturnVendorLineDO::getItemId));
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmReturnVendorLineRespVO.class, vo -> {
            MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                vo.setItemCode(item.getCode());
                vo.setItemName(item.getName());
                vo.setSpecification(item.getSpecification());
                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                        unitMeasure -> vo.setUnitMeasureName(unitMeasure.getName()));
            });
        });
    }

}
