package cn.iocoder.yudao.module.wms.controller.admin.outbound;

import cn.iocoder.yudao.framework.cola.statemachine.ApprovalReqVO;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import org.springframework.context.annotation.Lazy;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

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
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsOutboundRespVO::getCreator, WmsOutboundRespVO::setCreatorName)
			.mapping(WmsOutboundRespVO::getCreator, WmsOutboundRespVO::setUpdaterName)
			.fill();
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
    @PreAuthorize("@ss.hasPermission('wms:inbound:submit')")
    public CommonResult<Boolean> submit(@RequestBody ApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.SUBMIT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/agree")
    @Operation(summary = "同意审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:agree')")
    public CommonResult<Boolean> agree(@RequestBody ApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.AGREE, approvalReqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "驳回审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:reject')")
    public CommonResult<Boolean> reject(@RequestBody ApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.REJECT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成出库")
    @PreAuthorize("@ss.hasPermission('wms:inbound:finish')")
    public CommonResult<Boolean> finish(@RequestBody ApprovalReqVO approvalReqVO) {
        outboundService.approve(WmsOutboundAuditStatus.Event.FINISH, approvalReqVO);
        return success(true);
    }
}
