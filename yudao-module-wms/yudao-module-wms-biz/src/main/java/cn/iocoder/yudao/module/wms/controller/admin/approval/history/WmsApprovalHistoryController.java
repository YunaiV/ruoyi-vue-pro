package cn.iocoder.yudao.module.wms.controller.admin.approval.history;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.approval.history.WmsApprovalHistoryDO;
import cn.iocoder.yudao.module.wms.service.approval.history.WmsApprovalHistoryService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.APPROVAL_HISTORY_NOT_EXISTS;

@Tag(name = "审批历史")
@RestController
@RequestMapping("/wms/approval-history")
@Validated
public class WmsApprovalHistoryController {

    @Resource
    private WmsApprovalHistoryService approvalHistoryService;

    // /**
    // * @sign : 3655A9938D11E84E
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建审批历史")
    // @PreAuthorize("@ss.hasPermission('wms:approval-history:create')")
    // public CommonResult<Long> createApprovalHistory(@Valid @RequestBody WmsApprovalHistorySaveReqVO createReqVO) {
    // return success(approvalHistoryService.createApprovalHistory(createReqVO).getId());
    // }
    // /**
    // * @sign : 8EB272B59BCEA5B7
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新审批历史")
    // @PreAuthorize("@ss.hasPermission('wms:approval-history:update')")
    // public CommonResult<Boolean> updateApprovalHistory(@Valid @RequestBody WmsApprovalHistorySaveReqVO updateReqVO) {
    // approvalHistoryService.updateApprovalHistory(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除审批历史")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:approval-history:delete')")
    // public CommonResult<Boolean> deleteApprovalHistory(@RequestParam("id") Long id) {
    // approvalHistoryService.deleteApprovalHistory(id);
    // return success(true);
    // }
    /**
     * @sign : 2EACE20AECAC531F
     */
    @GetMapping("/get")
    @Operation(summary = "获得审批历史")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:approval-history:query')")
    public CommonResult<WmsApprovalHistoryRespVO> getApprovalHistory(@RequestParam("id") Long id) {
        // 查询数据
        WmsApprovalHistoryDO approvalHistory = approvalHistoryService.getApprovalHistory(id);
        if (approvalHistory == null) {
            throw exception(APPROVAL_HISTORY_NOT_EXISTS);
        }
        // 转换
        WmsApprovalHistoryRespVO approvalHistoryVO = BeanUtils.toBean(approvalHistory, WmsApprovalHistoryRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(approvalHistoryVO))
			.mapping(WmsApprovalHistoryRespVO::getCreator, WmsApprovalHistoryRespVO::setCreatorName)
			.mapping(WmsApprovalHistoryRespVO::getUpdater, WmsApprovalHistoryRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(approvalHistoryVO);
    }

    /**
     * @sign : 8BC28E50803219DB
     */
    @GetMapping("/page")
    @Operation(summary = "获得审批历史分页")
    @PreAuthorize("@ss.hasPermission('wms:approval-history:query')")
    public CommonResult<PageResult<WmsApprovalHistoryRespVO>> getApprovalHistoryPage(@Valid WmsApprovalHistoryPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsApprovalHistoryDO> doPageResult = approvalHistoryService.getApprovalHistoryPage(pageReqVO);
        // 转换
        PageResult<WmsApprovalHistoryRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsApprovalHistoryRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsApprovalHistoryRespVO::getCreator, WmsApprovalHistoryRespVO::setCreatorName)
			.mapping(WmsApprovalHistoryRespVO::getUpdater, WmsApprovalHistoryRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出审批历史 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:approval-history:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportApprovalHistoryExcel(@Valid WmsApprovalHistoryPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsApprovalHistoryDO> list = approvalHistoryService.getApprovalHistoryPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "审批历史.xls", "数据", WmsApprovalHistoryRespVO.class, BeanUtils.toBean(list, WmsApprovalHistoryRespVO.class));
    // }
}
