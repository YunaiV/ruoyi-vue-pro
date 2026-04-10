package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.wm.returnsales.MesWmReturnSalesService;
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

/**
 * 管理后台 - MES 销售退货单 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - MES 销售退货单")
@RestController
@RequestMapping("/mes/wm/return-sales")
@Validated
public class MesWmReturnSalesController {

    @Resource
    private MesWmReturnSalesService returnSalesService;
    @Resource
    private MesMdClientService clientService;

    @PostMapping("/create")
    @Operation(summary = "创建销售退货单")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:create')")
    public CommonResult<Long> createReturnSales(@Valid @RequestBody MesWmReturnSalesSaveReqVO createReqVO) {
        return success(returnSalesService.createReturnSales(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改销售退货单")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:update')")
    public CommonResult<Boolean> updateReturnSales(@Valid @RequestBody MesWmReturnSalesSaveReqVO updateReqVO) {
        returnSalesService.updateReturnSales(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:delete')")
    public CommonResult<Boolean> deleteReturnSales(@RequestParam("id") Long id) {
        returnSalesService.deleteReturnSales(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售退货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:query')")
    public CommonResult<MesWmReturnSalesRespVO> getReturnSales(@RequestParam("id") Long id) {
        MesWmReturnSalesDO returnSales = returnSalesService.getReturnSales(id);
        if (returnSales == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(returnSales)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售退货单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:query')")
    public CommonResult<PageResult<MesWmReturnSalesRespVO>> getReturnSalesPage(
            @Valid MesWmReturnSalesPageReqVO pageReqVO) {
        PageResult<MesWmReturnSalesDO> pageResult = returnSalesService.getReturnSalesPage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售退货单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReturnSalesExcel(@Valid MesWmReturnSalesPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmReturnSalesDO> pageResult = returnSalesService.getReturnSalesPage(pageReqVO);
        ExcelUtils.write(response, "销售退货单.xls", "数据", MesWmReturnSalesRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交销售退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:submit')")
    public CommonResult<Boolean> submitReturnSales(@RequestParam("id") Long id) {
        returnSalesService.submitReturnSales(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "执行退货")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:finish')")
    public CommonResult<Boolean> finishReturnSales(@RequestParam("id") Long id) {
        returnSalesService.finishReturnSales(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "执行上架")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:stock')")
    public CommonResult<Boolean> stockReturnSales(@RequestParam("id") Long id) {
        returnSalesService.stockReturnSales(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消销售退货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:cancel')")
    public CommonResult<Boolean> cancelReturnSales(@RequestParam("id") Long id) {
        returnSalesService.cancelReturnSales(id);
        return success(true);
    }

    @GetMapping("/check-quantity")
    @Operation(summary = "校验销售退货单数量", description = "校验每行明细数量之和是否等于行退货数量")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-return-sales:query')")
    public CommonResult<Boolean> checkReturnSalesQuantity(@RequestParam("id") Long id) {
        return success(returnSalesService.checkReturnSalesQuantity(id));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmReturnSalesRespVO> buildRespVOList(List<MesWmReturnSalesDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesWmReturnSalesDO::getClientId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmReturnSalesRespVO.class, vo ->
                MapUtils.findAndThen(clientMap, vo.getClientId(), client ->
                        vo.setClientCode(client.getCode()).setClientName(client.getName()).setClientNickname(client.getNickname())));
    }

}
