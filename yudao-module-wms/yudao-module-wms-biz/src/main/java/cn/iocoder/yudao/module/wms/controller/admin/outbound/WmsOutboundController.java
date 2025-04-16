package cn.iocoder.yudao.module.wms.controller.admin.outbound;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.List;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "出库单")
@RestController
@RequestMapping("/wms/outbound")
@Validated
public class WmsOutboundController {

    @Resource
    @Lazy
    private WmsOutboundItemService outboundItemService;

    @Resource
    private WmsOutboundService outboundService;

    /**
     * @sign : 3E40A4073A9BDC00
     */
    @PostMapping("/create")
    @Operation(summary = "创建出库单")
    @PreAuthorize("@ss.hasPermission('wms:outbound:create')")
    public CommonResult<Long> createOutbound(@Validated(ValidationGroup.create.class) @RequestBody WmsOutboundSaveReqVO createReqVO) {
        return success(outboundService.createOutbound(createReqVO).getId());
    }

    /**
     * @sign : EA6BB1E737DEB144
     */
    @PutMapping("/update")
    @Operation(summary = "更新出库单")
    @PreAuthorize("@ss.hasPermission('wms:outbound:update')")
    public CommonResult<Boolean> updateOutbound(@Validated(ValidationGroup.update.class) @RequestBody WmsOutboundSaveReqVO updateReqVO) {
        outboundService.updateOutbound(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:outbound:delete')")
    public CommonResult<Boolean> deleteOutbound(@RequestParam("id") Long id) {
        outboundService.deleteOutbound(id);
        return success(true);
    }

    /**
     * @sign : 9C9A810A627D637A
     */
    @GetMapping("/get")
    @Operation(summary = "获得出库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:outbound:query')")
    public CommonResult<WmsOutboundRespVO> getOutbound(@RequestParam("id") Long id) {
        // 查询数据
        WmsOutboundRespVO outboundVO = outboundService.getOutboundWithItemList(id);
        // 装配出库仓位
        outboundItemService.assembleBin(outboundVO.getItemList());
        // 返回
        return success(outboundVO);
    }

    /**
     * @sign : 352DBC195CCFA967
     */
    @GetMapping("/page")
    @Operation(summary = "获得出库单分页")
    @PreAuthorize("@ss.hasPermission('wms:outbound:query')")
    public CommonResult<PageResult<WmsOutboundRespVO>> getOutboundPage(@Valid WmsOutboundPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsOutboundDO> doPageResult = outboundService.getOutboundPage(pageReqVO);
        // 转换
        PageResult<WmsOutboundRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsOutboundRespVO.class);
        outboundService.assembleWarehouse(voPageResult.getList());
        outboundService.assembleDept(voPageResult.getList());
        outboundService.assembleCompany(voPageResult.getList());
        outboundService.assembleApprovalHistory(voPageResult.getList());
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsOutboundRespVO::getCreator, WmsOutboundRespVO::setCreatorName)
			.mapping(WmsOutboundRespVO::getUpdater, WmsOutboundRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    /**
     * @sign : 352DBC195CCFA967
     */
    @GetMapping("/simple-list")
    @Operation(summary = "获得出库单精简列表")
    @PreAuthorize("@ss.hasPermission('wms:outbound:query')")
    public CommonResult<List<WmsOutboundSimpleRespVO>> getOutboundSimpleList(@Valid WmsOutboundPageReqVO pageReqVO) {
        // 查询数据
        List<WmsOutboundDO> doPageResult = outboundService.getSimpleList(pageReqVO);
        // 转换
        List<WmsOutboundSimpleRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsOutboundSimpleRespVO.class);
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:outbound:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutboundExcel(@Valid WmsOutboundPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsOutboundDO> list = outboundService.getOutboundPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出库单.xls", "数据", WmsOutboundRespVO.class, BeanUtils.toBean(list, WmsOutboundRespVO.class));
    }

    @PutMapping("/submit")
    @Operation(summary = "提交审批")
    @PreAuthorize("@ss.hasPermission('wms:outbound:submit')")
    public CommonResult<Boolean> submit(@RequestBody WmsApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.SUBMIT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/agree")
    @Operation(summary = "同意审批")
    @PreAuthorize("@ss.hasPermission('wms:outbound:agree')")
    public CommonResult<Boolean> agree(@RequestBody WmsApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.AGREE, approvalReqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "驳回审批")
    @PreAuthorize("@ss.hasPermission('wms:outbound:reject')")
    public CommonResult<Boolean> reject(@RequestBody WmsApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.REJECT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成出库")
    @PreAuthorize("@ss.hasPermission('wms:outbound:finish')")
    public CommonResult<Boolean> finish(@RequestBody WmsApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.FINISH, approvalReqVO);
        return success(true);
    }
}