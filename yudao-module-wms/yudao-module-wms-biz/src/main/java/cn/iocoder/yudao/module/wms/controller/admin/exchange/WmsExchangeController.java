package cn.iocoder.yudao.module.wms.controller.admin.exchange;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectiveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.module.wms.service.exchange.WmsExchangeService;
import cn.iocoder.yudao.module.wms.service.exchange.defective.WmsExchangeDefectiveService;
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
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Tag(name = "换货单")
@RestController
@RequestMapping("/wms/exchange")
@Validated
public class WmsExchangeController {

    @Resource()
    @Lazy()
    private WmsExchangeDefectiveService exchangeDefectiveService;

    @Resource
    private WmsExchangeService exchangeService;

    /**
     * @sign : 13DA92746B9566CD
     */
    @PostMapping("/create")
    @Operation(summary = "创建换货单")
    @PreAuthorize("@ss.hasPermission('wms:exchange:create')")
    public CommonResult<Long> createExchange(@Validated(ValidationGroup.create.class) @RequestBody WmsExchangeSaveReqVO createReqVO) {
        return success(exchangeService.createExchange(createReqVO).getId());
    }

    /**
     * @sign : E7495B97D3ED6CDC
     */
    @PutMapping("/update")
    @Operation(summary = "更新换货单")
    @PreAuthorize("@ss.hasPermission('wms:exchange:update')")
    public CommonResult<Boolean> updateExchange(@Validated(ValidationGroup.update.class) @RequestBody WmsExchangeSaveReqVO updateReqVO) {
        exchangeService.updateExchange(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除换货单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:exchange:delete')")
    public CommonResult<Boolean> deleteExchange(@RequestParam("id") Long id) {
        exchangeService.deleteExchange(id);
        return success(true);
    }

    /**
     * @sign : 7A48B99F1BC3F24A
     */
    @GetMapping("/get")
    @Operation(summary = "获得换货单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:exchange:query')")
    public CommonResult<WmsExchangeRespVO> getExchange(@RequestParam("id") Long id) {
        // 查询数据
        WmsExchangeDO exchange = exchangeService.getExchange(id);
        if (exchange == null) {
            throw exception(EXCHANGE_NOT_EXISTS);
        }
        // 转换
        WmsExchangeRespVO exchangeVO = BeanUtils.toBean(exchange, WmsExchangeRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(exchangeVO))
			.mapping(WmsExchangeRespVO::getCreator, WmsExchangeRespVO::setCreatorName)
			.mapping(WmsExchangeRespVO::getCreator, WmsExchangeRespVO::setUpdaterName)
			.fill();
        // 组装良次换货详情
        List<WmsExchangeDefectiveDO> exchangeDefectiveList = exchangeDefectiveService.selectByExchangeId(exchangeVO.getId());
        exchangeVO.setDefectiveList(BeanUtils.toBean(exchangeDefectiveList, WmsExchangeDefectiveRespVO.class));
        // 返回
        return success(exchangeVO);
    }

    /**
     * @sign : 053E99C848B97F4B
     */
    @PostMapping("/page")
    @Operation(summary = "获得换货单分页")
    @PreAuthorize("@ss.hasPermission('wms:exchange:query')")
    public CommonResult<PageResult<WmsExchangeRespVO>> getExchangePage(@Valid @RequestBody WmsExchangePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsExchangeDO> doPageResult = exchangeService.getExchangePage(pageReqVO);
        // 转换
        PageResult<WmsExchangeRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsExchangeRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsExchangeRespVO::getCreator, WmsExchangeRespVO::setCreatorName)
			.mapping(WmsExchangeRespVO::getCreator, WmsExchangeRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出换货单 Excel")
    @PreAuthorize("@ss.hasPermission('wms:exchange:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportExchangeExcel(@Valid WmsExchangePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsExchangeDO> list = exchangeService.getExchangePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "换货单.xls", "数据", WmsExchangeRespVO.class, BeanUtils.toBean(list, WmsExchangeRespVO.class));
    }
}