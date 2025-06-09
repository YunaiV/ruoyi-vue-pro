package cn.iocoder.yudao.module.wms.controller.admin.stockcheck;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.stockcheck.WmsStockCheckService;
import cn.iocoder.yudao.module.wms.service.stockcheck.bin.WmsStockCheckBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.STOCKCHECK_NOT_EXISTS;

@Tag(name = "盘点单")
@RestController
@RequestMapping("/wms/stock-check")
@Validated
public class WmsStockCheckController {

    @Resource()
    @Lazy()
    private WmsStockCheckBinService stockCheckBinService;


    @Resource
    private WmsStockCheckService stockCheckService;

    /**
     * @sign : EEF1FA4365B38CB4
     */
    @PostMapping("/create")
    @Operation(summary = "盘点单创建")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:create')")
    public CommonResult<Long> createStockCheck(@Valid @RequestBody WmsStockCheckSaveReqVO createReqVO) {
        return success(stockCheckService.createStockCheck(createReqVO).getId());
    }

//    /**
//     * @sign : 30CB28F31026826D
//     */
//    @PutMapping("/update")
//    @Operation(summary = "盘点单更新")
//    @PreAuthorize("@ss.hasPermission('wms:stock-check:update')")
//    public CommonResult<Boolean> updateStockCheck(@Valid @RequestBody WmsStockCheckSaveReqVO updateReqVO) {
//        stockCheckService.updateStockCheck(updateReqVO);
//        return success(true);
//    }

    @DeleteMapping("/delete")
    @Operation(summary = "盘点单删除")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-check:delete')")
    public CommonResult<Boolean> deleteStockCheck(@RequestParam("id") Long id) {
        stockCheckService.deleteStockCheck(id);
        return success(true);
    }

    /**
     * @sign : FD03427E08081E43
     */
    @GetMapping("/get")
    @Operation(summary = "盘点单获得详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:query')")
    public CommonResult<WmsStockCheckRespVO> getStockCheck(@RequestParam("id") Long id) {
        // 查询数据
        WmsStockCheckDO stockCheck = stockCheckService.getStockCheck(id);
        if (stockCheck == null) {
            throw exception(STOCKCHECK_NOT_EXISTS);
        }
        // 转换
        WmsStockCheckRespVO stockCheckVO = BeanUtils.toBean(stockCheck, WmsStockCheckRespVO.class);
        // 组装库位盘点
        List<WmsStockCheckBinDO> stockCheckBinList = stockCheckBinService.selectByStockCheckId(stockCheckVO.getId());
        stockCheckVO.setBinItemList(BeanUtils.toBean(stockCheckBinList, WmsStockCheckBinRespVO.class));
        // 装配
        stockCheckService.assembleWarehouse(List.of(stockCheckVO));
        stockCheckService.assembleApprovalHistory(List.of(stockCheckVO));
        stockCheckBinService.assembleProduct(stockCheckVO.getBinItemList());
        stockCheckBinService.assembleBin(stockCheckVO.getBinItemList());

        for (WmsStockCheckBinRespVO stockCheckBinRespVO : stockCheckVO.getBinItemList()) {
            stockCheckBinRespVO.setDeltaQty(Math.abs(stockCheckBinRespVO.getActualQty() - stockCheckBinRespVO.getExpectedQty()));
        }

        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(stockCheckVO))
            .mapping(WmsStockCheckRespVO::getCreator, WmsStockCheckRespVO::setCreatorName)
            .mapping(WmsStockCheckRespVO::getUpdater, WmsStockCheckRespVO::setUpdaterName)
            .fill();

        // 人员姓名填充
        AdminUserApi.inst().prepareFill(stockCheckVO.getBinItemList())
            .mapping(WmsStockCheckBinRespVO::getCreator, WmsStockCheckBinRespVO::setCreatorName)
            .mapping(WmsStockCheckBinRespVO::getUpdater, WmsStockCheckBinRespVO::setUpdaterName)
            .fill();
        // 返回
        return success(stockCheckVO);
    }

    /**
     * @sign : B1908BBFDE62B4FB
     */
    @PostMapping("/page")
    @Operation(summary = "盘点单获得分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:query')")
    public CommonResult<PageResult<WmsStockCheckRespVO>> getStockCheckPage(@Valid @RequestBody WmsStockCheckPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockCheckDO> doPageResult = stockCheckService.getStockCheckPage(pageReqVO);
        // 转换
        PageResult<WmsStockCheckRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockCheckRespVO.class);
        // 装配
        stockCheckService.assembleWarehouse(voPageResult.getList());
        stockCheckService.assembleApprovalHistory(voPageResult.getList());
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
            .mapping(WmsStockCheckRespVO::getCreator, WmsStockCheckRespVO::setCreatorName)
            .mapping(WmsStockCheckRespVO::getUpdater, WmsStockCheckRespVO::setUpdaterName)
            .fill();

        // 返回
        return success(voPageResult);
    }

//    @GetMapping("/export-excel")
//    @Operation(summary = "导出盘点 Excel")
//    @PreAuthorize("@ss.hasPermission('wms:stock-check:export')")
//    @ApiAccessLog(operateType = EXPORT)
//    public void exportStockCheckExcel(@Valid WmsStockCheckPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<WmsStockCheckDO> list = stockCheckService.getStockCheckPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "盘点.xls", "数据", WmsStockCheckRespVO.class, BeanUtils.toBean(list, WmsStockCheckRespVO.class));
//    }

    @PutMapping("/submit")
    @Operation(summary = "盘点单提交审批")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:submit')")
    public CommonResult<Boolean> submit(@RequestBody WmsApprovalReqVO approvalReqVO) {
        stockCheckService.approve(WmsStockCheckAuditStatus.Event.SUBMIT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/agree")
    @Operation(summary = "盘点单同意审批")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:agree')")
    public CommonResult<Boolean> agree(@RequestBody WmsApprovalReqVO approvalReqVO) {
        stockCheckService.approve(WmsStockCheckAuditStatus.Event.AGREE, approvalReqVO);
        return success(true);
    }

    @PutMapping("/abandon")
    @Operation(summary = "盘点单作废")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:abandon')")
    public CommonResult<Boolean> abandon(@RequestBody WmsApprovalReqVO approvalReqVO) {
        stockCheckService.approve(WmsStockCheckAuditStatus.Event.ABANDON, approvalReqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "盘点单驳回审批")
    @PreAuthorize("@ss.hasPermission('wms:stock-check:reject')")
    public CommonResult<Boolean> reject(@RequestBody WmsApprovalReqVO approvalReqVO) {
        stockCheckService.approve(WmsStockCheckAuditStatus.Event.REJECT, approvalReqVO);
        return success(true);
    }
}
