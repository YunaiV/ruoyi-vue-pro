package cn.iocoder.yudao.module.wms.controller.admin.exchange.item;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo.WmsExchangeItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import cn.iocoder.yudao.module.wms.service.exchange.item.WmsExchangeItemService;
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

/**
 * @author jisencai
 */
@Tag(name = "良次换货详情")
@RestController
@RequestMapping("/wms/exchange-item")
@Validated
public class WmsExchangeItemController {

    @Resource
    private WmsExchangeItemService exchangeItemService;

    // /**
    // * @sign : 98E7231CA1CD41C5
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建良次换货详情")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-item:create')")
    // public CommonResult<Long> createExchangeItem(@Validated(ValidationGroup.create.class) @RequestBody WmsExchangeItemSaveReqVO createReqVO) {
    // return success(exchangeItemService.createExchangeItem(createReqVO).getId());
    // }
    // 
    // /**
    // * @sign : 5209EB09333C4C84
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新良次换货详情")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-item:update')")
    // public CommonResult<Boolean> updateExchangeItem(@Validated(ValidationGroup.update.class) @RequestBody WmsExchangeItemSaveReqVO updateReqVO) {
    // exchangeItemService.updateExchangeItem(updateReqVO);
    // return success(true);
    // }
    // 
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除良次换货详情")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:exchange-item:delete')")
    // public CommonResult<Boolean> deleteExchangeItem(@RequestParam("id") Long id) {
    // exchangeItemService.deleteExchangeItem(id);
    // return success(true);
    // }
    // 
    // /**
    // * @sign : AD8AE86AA6C9D19D
    // */
    // @GetMapping("/get")
    // @Operation(summary = "获得良次换货详情")
    // @Parameter(name = "id", description = "编号", required = true, example = "1024")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-item:query')")
    // public CommonResult<WmsExchangeItemRespVO> getExchangeItem(@RequestParam("id") Long id) {
    // // 查询数据
    // WmsExchangeItemDO exchangeItem = exchangeItemService.getExchangeItem(id);
    // if (exchangeItem == null) {
    // throw exception(EXCHANGE_ITEM_NOT_EXISTS);
    // }
    // // 转换
    // WmsExchangeItemRespVO exchangeItemVO = BeanUtils.toBean(exchangeItem, WmsExchangeItemRespVO.class);
    // // 人员姓名填充
    // AdminUserApi.inst().prepareFill(List.of(exchangeItemVO))
    // .mapping(WmsExchangeItemRespVO::getCreator, WmsExchangeItemRespVO::setCreatorName)
    // .mapping(WmsExchangeItemRespVO::getCreator, WmsExchangeItemRespVO::setUpdaterName)
    // .fill();
    // // 返回
    // return success(exchangeItemVO);
    // }

    /**
     * @sign : B8E9A441A24A8AE5
     */
    @PostMapping("/page")
    @Operation(summary = "获得良次换货详情分页")
    @PreAuthorize("@ss.hasPermission('wms:exchange-item:query')")
    public CommonResult<PageResult<WmsExchangeItemRespVO>> getExchangeItemPage(@Valid @RequestBody WmsExchangeItemPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsExchangeItemDO> doPageResult = exchangeItemService.getExchangeItemPage(pageReqVO);
        // 转换
        PageResult<WmsExchangeItemRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsExchangeItemRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
            .mapping(WmsExchangeItemRespVO::getCreator, WmsExchangeItemRespVO::setCreatorName)
            .mapping(WmsExchangeItemRespVO::getCreator, WmsExchangeItemRespVO::setUpdaterName)
            .fill();
        // 装配
        exchangeItemService.assembleBins(voPageResult.getList());
        exchangeItemService.assembleProduct(voPageResult.getList());
        // 返回
        return success(voPageResult);
    }
    // @GetMapping("/export-excel")
    // @Operation(summary = "导出良次换货详情 Excel")
    // @PreAuthorize("@ss.hasPermission('wms:exchange-item:export')")
    // @ApiAccessLog(operateType = EXPORT)
    // public void exportExchangeItemExcel(@Valid WmsExchangeItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
    // pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
    // List<WmsExchangeItemDO> list = exchangeItemService.getExchangeItemPage(pageReqVO).getList();
    // // 导出 Excel
    // ExcelUtils.write(response, "良次换货详情.xls", "数据", WmsExchangeItemRespVO.class, BeanUtils.toBean(list, WmsExchangeItemRespVO.class));
    // }
}
