package cn.iocoder.yudao.module.wms.controller.admin.inbound.item;

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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "入库单详情")
@RestController
@RequestMapping("/wms/inbound-item")
@Validated
public class WmsInboundItemController {

    @Resource
    private WmsInboundItemService inboundItemService;

    /**
     * @sign : B115F14578BE657A
     */
    @PostMapping("/create")
    @Operation(summary = "创建入库单详情")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:create')")
    public CommonResult<Long> createInboundItem(@Valid @RequestBody WmsInboundItemSaveReqVO createReqVO) {
        return success(inboundItemService.createInboundItem(createReqVO).getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新入库单详情")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:update')")
    public CommonResult<Boolean> updateInboundItem(@Valid @RequestBody WmsInboundItemSaveReqVO updateReqVO) {
        inboundItemService.updateInboundItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除入库单详情")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:delete')")
    public CommonResult<Boolean> deleteInboundItem(@RequestParam("id") Long id) {
        inboundItemService.deleteInboundItem(id);
        return success(true);
    }

    /**
     * @sign : F0DA74F2E45ABE2D
     */
    @GetMapping("/get")
    @Operation(summary = "获得入库单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<WmsInboundItemRespVO> getInboundItem(@RequestParam("id") Long id) {
        // 查询数据
        WmsInboundItemDO inboundItem = inboundItemService.getInboundItem(id);
        if (inboundItem == null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        // 转换
        WmsInboundItemRespVO inboundItemVO = BeanUtils.toBean(inboundItem, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(inboundItemVO))
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(inboundItemVO);
    }

    /**
     * @sign : 83456B9A2BFF8F84
     */
    @GetMapping("/page")
    @Operation(summary = "获得入库单详情分页")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:query')")
    public CommonResult<PageResult<WmsInboundItemRespVO>> getInboundItemPage(@Valid WmsInboundItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsInboundItemDO> doPageResult = inboundItemService.getInboundItemPage(pageReqVO);
        // 转换
        PageResult<WmsInboundItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setCreatorName)
			.mapping(WmsInboundItemRespVO::getCreator, WmsInboundItemRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出入库单详情 Excel")
    @PreAuthorize("@ss.hasPermission('wms:inbound-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportInboundItemExcel(@Valid WmsInboundItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsInboundItemDO> list = inboundItemService.getInboundItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "入库单详情.xls", "数据", WmsInboundItemRespVO.class, BeanUtils.toBean(list, WmsInboundItemRespVO.class));
    }
}