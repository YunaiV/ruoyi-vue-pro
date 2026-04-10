package cn.iocoder.yudao.module.mes.controller.admin.wm.productissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.MesWmProductIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.MesWmProductIssueRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.MesWmProductIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.productissue.MesWmProductIssueService;
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

@Tag(name = "管理后台 - MES 领料出库单")
@RestController
@RequestMapping("/mes/wm/product-issue")
@Validated
public class MesWmProductIssueController {

    @Resource
    private MesWmProductIssueService issueService;
    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesMdClientService clientService;

    @PostMapping("/create")
    @Operation(summary = "创建领料出库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:create')")
    public CommonResult<Long> createProductIssue(@Valid @RequestBody MesWmProductIssueSaveReqVO createReqVO) {
        return success(issueService.createProductIssue(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改领料出库单")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:update')")
    public CommonResult<Boolean> updateProductIssue(@Valid @RequestBody MesWmProductIssueSaveReqVO updateReqVO) {
        issueService.updateProductIssue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除领料出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:delete')")
    public CommonResult<Boolean> deleteProductIssue(@RequestParam("id") Long id) {
        issueService.deleteProductIssue(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得领料出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:query')")
    public CommonResult<MesWmProductIssueRespVO> getProductIssue(@RequestParam("id") Long id) {
        MesWmProductIssueDO issue = issueService.getProductIssue(id);
        if (issue == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(issue)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得领料出库单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:query')")
    public CommonResult<PageResult<MesWmProductIssueRespVO>> getProductIssuePage(
            @Valid MesWmProductIssuePageReqVO pageReqVO) {
        PageResult<MesWmProductIssueDO> pageResult = issueService.getProductIssuePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出领料出库单 Excel")
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductIssueExcel(@Valid MesWmProductIssuePageReqVO pageReqVO,
                                            HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MesWmProductIssueDO> pageResult = issueService.getProductIssuePage(pageReqVO);
        ExcelUtils.write(response, "领料出库单.xls", "数据", MesWmProductIssueRespVO.class,
                buildRespVOList(pageResult.getList()));
    }

    @PutMapping("/finish")
    @Operation(summary = "完成领料出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:finish')")
    public CommonResult<Boolean> finishProductIssue(@RequestParam("id") Long id) {
        issueService.finishProductIssue(id);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交领料出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:update')")
    public CommonResult<Boolean> submitProductIssue(@RequestParam("id") Long id) {
        issueService.submitProductIssue(id);
        return success(true);
    }

    @PutMapping("/stock")
    @Operation(summary = "执行拣货")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:update')")
    public CommonResult<Boolean> stockProductIssue(@RequestParam("id") Long id) {
        issueService.stockProductIssue(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消领料出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:update')")
    public CommonResult<Boolean> cancelProductIssue(@RequestParam("id") Long id) {
        issueService.cancelProductIssue(id);
        return success(true);
    }

    @GetMapping("/check-quantity")
    @Operation(summary = "校验领料出库单数量", description = "校验每行明细数量之和是否等于行领料数量")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-product-issue:query')")
    public CommonResult<Boolean> checkProductIssueQuantity(@RequestParam("id") Long id) {
        return success(issueService.checkProductIssueQuantity(id));
    }

    // ==================== 拼接 VO ====================

    private List<MesWmProductIssueRespVO> buildRespVOList(List<MesWmProductIssueDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 获得关联数据
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesWmProductIssueDO::getWorkstationId));
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesWmProductIssueDO::getWorkOrderId));
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(workOrderMap.values(), MesProWorkOrderDO::getClientId));
        // 2. 构建结果
        return BeanUtils.toBean(list, MesWmProductIssueRespVO.class, vo -> {
            MapUtils.findAndThen(workstationMap, vo.getWorkstationId(),
                    workstation -> vo.setWorkstationName(workstation.getName()));
            MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(), workOrder -> {
                vo.setWorkOrderCode(workOrder.getCode());
                MapUtils.findAndThen(clientMap, workOrder.getClientId(), client ->
                        vo.setClientCode(client.getCode()).setClientName(client.getName()));
            });
        });
    }

}
