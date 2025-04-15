package cn.iocoder.yudao.module.wms.controller.admin.inbound;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.WmsApprovalHistoryService;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Arrays;
import java.util.List;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

    @Autowired
    private WmsApprovalHistoryService wmsApprovalHistoryService;

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
        inboundService.approve(WmsInboundAuditStatus.Event.SUBMIT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/agree")
    @Operation(summary = "同意审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:agree')")
    public CommonResult<Boolean> agree(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(WmsInboundAuditStatus.Event.AGREE, approvalReqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "驳回审批")
    @PreAuthorize("@ss.hasPermission('wms:inbound:reject')")
    public CommonResult<Boolean> reject(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(WmsInboundAuditStatus.Event.REJECT, approvalReqVO);
        return success(true);
    }

    @PutMapping("/force-finish")
    @Operation(summary = "强制完成")
    @PreAuthorize("@ss.hasPermission('wms:inbound:force-finish')")
    public CommonResult<Boolean> forceFinish(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(WmsInboundAuditStatus.Event.FORCE_FINISH, approvalReqVO);
        return success(true);
    }

    @PutMapping("/abandon")
    @Operation(summary = "作废")
    @PreAuthorize("@ss.hasPermission('wms:inbound:abandon')")
    public CommonResult<Boolean> abandon(@RequestBody WmsApprovalReqVO approvalReqVO) {
        inboundService.approve(WmsInboundAuditStatus.Event.ABANDON, approvalReqVO);
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
        inboundService.assembleApprovalHistory(Arrays.asList(inboundVO));
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
        // 装配
        inboundService.assembleWarehouse(voPageResult.getList());
        inboundService.assembleCompany(voPageResult.getList());
        inboundService.assembleApprovalHistory(voPageResult.getList());
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setCreatorName)
			.mapping(WmsInboundRespVO::getUpdater, WmsInboundRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    /**
     * @sign : 1F430B4B7632C52B
     */
    @GetMapping("/simple-list")
    @Operation(summary = "获得入库精简列表")
    @PreAuthorize("@ss.hasPermission('wms:inbound:query')")
    public CommonResult<List<WmsInboundSimpleRespVO>> getInboundSimpleList(@Valid WmsInboundPageReqVO pageReqVO) {
        // 查询数据
        List<WmsInboundDO> doList = inboundService.getSimpleList(pageReqVO);
        // 转换
        List<WmsInboundSimpleRespVO> voList = BeanUtils.toBean(doList, WmsInboundSimpleRespVO.class);
        return success(voList);
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
