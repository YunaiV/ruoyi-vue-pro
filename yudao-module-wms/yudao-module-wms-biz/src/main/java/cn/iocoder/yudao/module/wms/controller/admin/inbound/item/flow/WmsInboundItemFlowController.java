package cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow;

import cn.iocoder.yudao.module.wms.service.inbound.item.flow.WmsItemFlowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jisencai
 */
@Tag(name = "入库单库存详情扣减")
@RestController
@RequestMapping("/wms/inbound-item-flow")
@Validated
public class WmsInboundItemFlowController {

    @Resource
    private WmsItemFlowService itemFlowService;

//    /**
//     * @sign : DF23AF4DA093DBC0
//     */
//    @PostMapping("/create")
//    @Operation(summary = "创建入库单库存详情扣减")
//    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:create')")
//    public CommonResult<Long> createInboundItemFlow(@Valid @RequestBody WmsInboundItemFlowSaveReqVO createReqVO) {
//        return success(itemFlowService.createInboundItemFlow(createReqVO).getId());
//    }
//
//    /**
//     * @sign : 611DCC9C3D746175
//     */
//    @PutMapping("/update")
//    @Operation(summary = "更新入库单库存详情扣减")
//    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:update')")
//    public CommonResult<Boolean> updateInboundItemFlow(@Valid @RequestBody WmsInboundItemFlowSaveReqVO updateReqVO) {
//        itemFlowService.updateInboundItemFlow(updateReqVO);
//        return success(true);
//    }
//
//    @DeleteMapping("/delete")
//    @Operation(summary = "删除入库单库存详情扣减")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:delete')")
//    public CommonResult<Boolean> deleteInboundItemFlow(@RequestParam("id") Long id) {
//        itemFlowService.deleteInboundItemFlow(id);
//        return success(true);
//    }
//
//    /**
//     * @sign : AB4B4736E8BF767C
//     */
//    @GetMapping("/get")
//    @Operation(summary = "获得入库单库存详情扣减")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:query')")
//    public CommonResult<WmsInboundItemFlowRespVO> getInboundItemFlow(@RequestParam("id") Long id) {
//        // 查询数据
//        WmsItemFlowDO inboundItemFlow = itemFlowService.getInboundItemFlow(id);
//        if (inboundItemFlow == null) {
//            throw exception(INBOUND_ITEM_FLOW_NOT_EXISTS);
//        }
//        // 转换
//        WmsInboundItemFlowRespVO inboundItemFlowVO = BeanUtils.toBean(inboundItemFlow, WmsInboundItemFlowRespVO.class);
//        // 人员姓名填充
//        AdminUserApi.inst().prepareFill(List.of(inboundItemFlowVO))
//			.mapping(WmsInboundItemFlowRespVO::getCreator, WmsInboundItemFlowRespVO::setCreatorName)
//			.mapping(WmsInboundItemFlowRespVO::getCreator, WmsInboundItemFlowRespVO::setUpdaterName)
//			.fill();
//        // 返回
//        return success(inboundItemFlowVO);
//    }
//
//    /**
//     * @sign : E340714EA441913D
//     */
//    @GetMapping("/page")
//    @Operation(summary = "获得入库单库存详情扣减分页")
//    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:query')")
//    public CommonResult<PageResult<WmsInboundItemFlowRespVO>> getInboundItemFlowPage(@Valid WmsInboundItemFlowPageReqVO pageReqVO) {
//        // 查询数据
//        PageResult<WmsItemFlowDO> doPageResult = itemFlowService.getInboundItemFlowPage(pageReqVO);
//        // 转换
//        PageResult<WmsInboundItemFlowRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsInboundItemFlowRespVO.class);
//        // 人员姓名填充
//        AdminUserApi.inst().prepareFill(voPageResult.getList())
//			.mapping(WmsInboundItemFlowRespVO::getCreator, WmsInboundItemFlowRespVO::setCreatorName)
//			.mapping(WmsInboundItemFlowRespVO::getCreator, WmsInboundItemFlowRespVO::setUpdaterName)
//			.fill();
//        // 返回
//        return success(voPageResult);
//    }
//
//    @GetMapping("/export-excel")
//    @Operation(summary = "导出入库单库存详情扣减 Excel")
//    @PreAuthorize("@ss.hasPermission('wms:inbound-item-flow:export')")
//    @ApiAccessLog(operateType = EXPORT)
//    public void exportInboundItemFlowExcel(@Valid WmsInboundItemFlowPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<WmsItemFlowDO> list = itemFlowService.getInboundItemFlowPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "入库单库存详情扣减.xls", "数据", WmsInboundItemFlowRespVO.class, BeanUtils.toBean(list, WmsInboundItemFlowRespVO.class));
//    }
}
