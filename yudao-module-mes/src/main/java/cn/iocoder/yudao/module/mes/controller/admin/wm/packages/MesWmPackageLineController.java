package cn.iocoder.yudao.module.mes.controller.admin.wm.packages;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages.MesWmPackageLineDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.packages.MesWmPackageLineService;
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

/**
 * MES 装箱明细 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - MES 装箱明细")
@RestController
@RequestMapping("/mes/wm/package-line")
@Validated
public class MesWmPackageLineController {

        @Resource
        private MesWmPackageLineService packageLineService;
        @Resource
        private MesMdItemService itemService;
        @Resource
        private MesMdUnitMeasureService unitMeasureService;
        @Resource
        private MesProWorkOrderService workOrderService;

        @PostMapping("/create")
        @Operation(summary = "创建装箱明细")
        @PreAuthorize("@ss.hasPermission('mes:wm-package:create')")
        public CommonResult<Long> createPackageLine(@Valid @RequestBody MesWmPackageLineSaveReqVO createReqVO) {
                return success(packageLineService.createPackageLine(createReqVO));
        }

        @PutMapping("/update")
        @Operation(summary = "修改装箱明细")
        @PreAuthorize("@ss.hasPermission('mes:wm-package:update')")
        public CommonResult<Boolean> updatePackageLine(@Valid @RequestBody MesWmPackageLineSaveReqVO updateReqVO) {
                packageLineService.updatePackageLine(updateReqVO);
                return success(true);
        }

        @DeleteMapping("/delete")
        @Operation(summary = "删除装箱明细")
        @Parameter(name = "id", description = "编号", required = true)
        @PreAuthorize("@ss.hasPermission('mes:wm-package:delete')")
        public CommonResult<Boolean> deletePackageLine(@RequestParam("id") Long id) {
                packageLineService.deletePackageLine(id);
                return success(true);
        }

        @GetMapping("/get")
        @Operation(summary = "获得装箱明细")
        @Parameter(name = "id", description = "编号", required = true, example = "1024")
        @PreAuthorize("@ss.hasPermission('mes:wm-package:query')")
        public CommonResult<MesWmPackageLineRespVO> getPackageLine(@RequestParam("id") Long id) {
                MesWmPackageLineDO line = packageLineService.getPackageLine(id);
                if (line == null) {
                        return success(null);
                }
                return success(buildRespVOList(Collections.singletonList(line)).get(0));
        }

        @GetMapping("/page")
        @Operation(summary = "获得装箱明细分页")
        @PreAuthorize("@ss.hasPermission('mes:wm-package:query')")
        public CommonResult<PageResult<MesWmPackageLineRespVO>> getPackageLinePage(
                        @Valid MesWmPackageLinePageReqVO pageReqVO) {
                PageResult<MesWmPackageLineDO> pageResult = packageLineService.getPackageLinePage(pageReqVO);
                return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
        }

        // ========== 私有方法 ==========

        private List<MesWmPackageLineRespVO> buildRespVOList(List<MesWmPackageLineDO> list) {
                if (CollUtil.isEmpty(list)) {
                        return Collections.emptyList();
                }
                // 批量查询物料
                Map<Long, MesMdItemDO> itemMap = itemService
                                .getItemMap(convertSet(list, MesWmPackageLineDO::getItemId));
                // 批量查询计量单位
                Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                                convertSet(itemMap.values(), MesMdItemDO::getUnitMeasureId));
                // 批量查询工单
                Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                                convertSet(list, MesWmPackageLineDO::getWorkOrderId));
                // 拼接数据
                return BeanUtils.toBean(list, MesWmPackageLineRespVO.class, vo -> {
                        MapUtils.findAndThen(itemMap, vo.getItemId(), item -> {
                                vo.setItemCode(item.getCode()).setItemName(item.getName())
                                                .setSpecification(item.getSpecification());
                                MapUtils.findAndThen(unitMeasureMap, item.getUnitMeasureId(),
                                                unit -> vo.setUnitMeasureName(unit.getName()));
                        });
                        MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(),
                                        workOrder -> vo.setWorkOrderCode(workOrder.getCode())
                                                        .setBatchCode(workOrder.getBatchCode()));
                });
        }

}
