package cn.iocoder.yudao.module.wms.controller.admin.inbound;

import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.*;
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

@Tag(name = "入库单")
@RestController
@RequestMapping("/wms/inbound")
@Validated
public class WmsInboundController {

    @Resource
    private WmsInboundService inboundService;

    /**
     * @sign : 6D8CDB05543AE552
     */
    @PostMapping("/create")
    @Operation(summary = "创建入库单")
    @PreAuthorize("@ss.hasPermission('wms:inbound:create')")
    public CommonResult<Long> createInbound(@Valid @RequestBody WmsInboundSaveReqVO createReqVO) {
        return success(inboundService.createInbound(createReqVO).getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新入库单")
    @PreAuthorize("@ss.hasPermission('wms:inbound:update')")
    public CommonResult<Boolean> updateInbound(@Valid @RequestBody WmsInboundSaveReqVO updateReqVO) {
        inboundService.updateInbound(updateReqVO);
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
     * @sign : 4BF4A4392DD037B1
     */
    @GetMapping("/get")
    @Operation(summary = "获得入库单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound:query')")
    public CommonResult<WmsInboundRespVO> getInbound(@RequestParam("id") Long id) {
        // 查询数据
        WmsInboundDO inbound = inboundService.getInbound(id);
        if (inbound == null) {
            throw exception(INBOUND_NOT_EXISTS);
        }
        // 转换
        WmsInboundRespVO inboundVO = BeanUtils.toBean(inbound, WmsInboundRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(inboundVO))
			.mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setCreatorName)
			.mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setUpdaterName)
			.fill();
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
