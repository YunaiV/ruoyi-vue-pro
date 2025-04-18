package cn.iocoder.yudao.module.wms.controller.admin.exchange.defective;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectivePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectiveRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.module.wms.service.exchange.defective.WmsExchangeDefectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "良次换货详情")
@RestController
@RequestMapping("/wms/exchange-defective")
@Validated
public class WmsExchangeDefectiveController {

    @Resource
    private WmsExchangeDefectiveService exchangeDefectiveService;

    // /**
    // * @sign : 98E7231CA1CD41C5
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建良次换货详情")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-defective:create')")
    // public CommonResult<Long> createExchangeDefective(@Validated(ValidationGroup.create.class) @RequestBody WmsExchangeDefectiveSaveReqVO createReqVO) {
    // return success(exchangeDefectiveService.createExchangeDefective(createReqVO).getId());
    // }
    // 
    // /**
    // * @sign : 5209EB09333C4C84
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新良次换货详情")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-defective:update')")
    // public CommonResult<Boolean> updateExchangeDefective(@Validated(ValidationGroup.update.class) @RequestBody WmsExchangeDefectiveSaveReqVO updateReqVO) {
    // exchangeDefectiveService.updateExchangeDefective(updateReqVO);
    // return success(true);
    // }
    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除良次换货详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:exchange-defective:delete')")
    // public CommonResult<Boolean> deleteExchangeDefective(@RequestParam("id") Long id) {
    // exchangeDefectiveService.deleteExchangeDefective(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : AD8AE86AA6C9D19D
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得良次换货详情")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-defective:query')")
    // public CommonResult<WmsExchangeDefectiveRespVO> getExchangeDefective(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsExchangeDefectiveDO exchangeDefective = exchangeDefectiveService.getExchangeDefective(id);
    // if (exchangeDefective == null) {
    // throw exception(EXCHANGE_DEFECTIVE_NOT_EXISTS);
    // }
    // // 转换
    // WmsExchangeDefectiveRespVO exchangeDefectiveVO = BeanUtils.toBean(exchangeDefective, WmsExchangeDefectiveRespVO.class);
    // // 人员姓名填充
    // AdminUserApi.inst().prepareFill(List.of(exchangeDefectiveVO))
    // .mapping(WmsExchangeDefectiveRespVO::getCreator, WmsExchangeDefectiveRespVO::setCreatorName)
    // .mapping(WmsExchangeDefectiveRespVO::getCreator, WmsExchangeDefectiveRespVO::setUpdaterName)
    // .fill();
    // // 返回
    // return success(exchangeDefectiveVO);
    // }
    /**
     * @sign : B8E9A441A24A8AE5
     */
    @PostMapping("/page")
    @Operation(summary = "获得良次换货详情分页")
    @PreAuthorize("@ss.hasPermission('wms:exchange-defective:query')")
    public CommonResult<PageResult<WmsExchangeDefectiveRespVO>> getExchangeDefectivePage(@Valid @RequestBody WmsExchangeDefectivePageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsExchangeDefectiveDO> doPageResult = exchangeDefectiveService.getExchangeDefectivePage(pageReqVO);
        // 转换
        PageResult<WmsExchangeDefectiveRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsExchangeDefectiveRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
			.mapping(WmsExchangeDefectiveRespVO::getCreator, WmsExchangeDefectiveRespVO::setCreatorName)
			.mapping(WmsExchangeDefectiveRespVO::getCreator, WmsExchangeDefectiveRespVO::setUpdaterName)
			.fill();
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出良次换货详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-defective:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportExchangeDefectiveExcel(@Valid WmsExchangeDefectivePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsExchangeDefectiveDO> list = exchangeDefectiveService.getExchangeDefectivePage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "良次换货详情.xls", "数据", WmsExchangeDefectiveRespVO.class, BeanUtils.toBean(list, WmsExchangeDefectiveRespVO.class));
    // }
}