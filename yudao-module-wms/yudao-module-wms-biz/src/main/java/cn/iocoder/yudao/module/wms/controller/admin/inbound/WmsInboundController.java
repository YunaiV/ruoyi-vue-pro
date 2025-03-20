package cn.iocoder.yudao.module.wms.controller.admin.inbound;

import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.ErpProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import org.springframework.context.annotation.Lazy;
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
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_NOT_EXISTS;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "入库单")
@RestController
@RequestMapping("/wms/inbound")
@Validated
public class WmsInboundController {

    @Resource()
    @Lazy()
    private WmsInboundItemService inboundItemService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    /**
     * @sign : 9362CEB68950BDF7
     */
    @PostMapping("/create")
    @Operation(summary = "创建入库单")
    @PreAuthorize("@ss.hasPermission('wms:inbound:create')")
    public CommonResult<Long> createInbound(@Validated(ValidationGroup.create.class) @RequestBody WmsInboundSaveReqVO createReqVO) {
        return success(inboundService.createInbound(createReqVO).getId());
    }

    /**
     * @sign : 9264A3220A24AA4C
     */
    @PutMapping("/update")
    @Operation(summary = "更新入库单")
    @PreAuthorize("@ss.hasPermission('wms:inbound:update')")
    public CommonResult<Boolean> updateInbound(@Validated(ValidationGroup.update.class) @RequestBody WmsInboundSaveReqVO updateReqVO) {
        inboundService.updateInbound(updateReqVO);
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:submit')")
    public CommonResult<Boolean> submit(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(InboundAuditStatus.Event.SUBMIT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/agree")
    @Operation(summary = "同意审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:agree')")
    public CommonResult<Boolean> agree(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(InboundAuditStatus.Event.AGREE, approvalReqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "驳回审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:reject')")
    public CommonResult<Boolean> reject(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(InboundAuditStatus.Event.REJECT, approvalReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除入库单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inbound:delete')")
    public CommonResult<Boolean> deleteInbound(@RequestParam("id") Long id) {
        inboundService.deleteInbound(id);
        return success(true);
    }

    /**
     * @sign : E3DC7B482DCFE2C4
     */
    @GetMapping("/get")
    @Operation(summary = "获得入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound:query')")
    public CommonResult<WmsInboundRespVO> getInbound(@RequestParam("id") Long id) {
        WmsInboundRespVO inboundVO = inboundService.getInboundWithItemList(id);
        // 返回
        return success(inboundVO);
    }

    /**
     * @sign : 1F430B4B7632C52B
     */
    @GetMapping("/page")
    @Operation(summary = "获得入库单分页")
    @PreAuthorize("@ss.hasPermission('wms:inbound:query')")
    public CommonResult<PageResult<WmsInboundRespVO>> getInboundPage(@Valid WmsInboundPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInboundDO> doPageResult = inboundService.getInboundPage(pageReqVO);
        // 转换
        PageResult<WmsInboundRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setCreatorName)
			.mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出入库单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundExcel(@Valid WmsInboundPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInboundDO> list = inboundService.getInboundPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "入库单.xls", "数据", WmsInboundRespVO.class, BeanUtils.toBean(list, WmsInboundRespVO.class));
    }
}