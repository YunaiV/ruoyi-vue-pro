//package cn.iocoder.yudao.module.tms.controller.admin.transfer.item;
//
//import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
//import cn.iocoder.yudao.framework.common.pojo.CommonResult;
//import cn.iocoder.yudao.framework.common.pojo.PageParam;
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
//import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
//import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
//import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemPageReqVO;
//import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemRespVO;
//import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemSaveReqVO;
//import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
//import cn.iocoder.yudao.module.tms.service.transfer.item.TmsTransferItemService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.validation.Valid;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.util.List;
//
//import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
//import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
//import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
//
//@Tag(name = "管理后台 - 调拨单明细")
//@RestController
//@RequestMapping("/tms/transfer-item")
//@Validated
//public class TmsTransferItemController {
//
//    @Resource
//    private TmsTransferItemService transferItemService;
//
//    @PostMapping("/create")
//    @Operation(summary = "创建调拨单明细")
//    @Idempotent
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:create')")
//    public CommonResult<Long> createTransferItem(@Valid @RequestBody TmsTransferItemSaveReqVO createReqVO) {
//        return success(transferItemService.createTransferItem(createReqVO));
//    }
//
//    @PutMapping("/update")
//    @Operation(summary = "更新调拨单明细")
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:update')")
//    public CommonResult<Boolean> updateTransferItem(@Valid @RequestBody TmsTransferItemSaveReqVO updateReqVO) {
//        transferItemService.updateTransferItem(updateReqVO);
//        return success(true);
//    }
//
//    @DeleteMapping("/delete")
//    @Operation(summary = "删除调拨单明细")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:delete')")
//    public CommonResult<Boolean> deleteTransferItem(@RequestParam("id") Long id) {
//        transferItemService.deleteTransferItem(id);
//        return success(true);
//    }
//
//    @GetMapping("/get")
//    @Operation(summary = "获得调拨单明细")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:query')")
//    public CommonResult<TmsTransferItemRespVO> getTransferItem(@RequestParam("id") Long id) {
//        TmsTransferItemDO transferItem = transferItemService.getTransferItem(id);
//        return success(BeanUtils.toBean(transferItem, TmsTransferItemRespVO.class));
//    }
//
//    @GetMapping("/page")
//    @Operation(summary = "获得调拨单明细分页")
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:query')")
//    public CommonResult<PageResult<TmsTransferItemRespVO>> getTransferItemPage(@Valid TmsTransferItemPageReqVO pageReqVO) {
//        PageResult<TmsTransferItemDO> pageResult = transferItemService.getTransferItemPage(pageReqVO);
//        return success(BeanUtils.toBean(pageResult, TmsTransferItemRespVO.class));
//    }
//
//    @GetMapping("/export-excel")
//    @Operation(summary = "导出调拨单明细 Excel")
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:export')")
//    @ApiAccessLog(operateType = EXPORT)
//    public void exportTransferItemExcel(@Valid TmsTransferItemPageReqVO pageReqVO,
//                                        HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<TmsTransferItemDO> list = transferItemService.getTransferItemPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "调拨单明细.xls", "数据", TmsTransferItemRespVO.class,
//            BeanUtils.toBean(list, TmsTransferItemRespVO.class));
//    }
//
//    @PostMapping("/import-excel")
//    @Operation(summary = "导入调拨单明细 Excel")
//    @ApiAccessLog(operateType = IMPORT)
//    @PreAuthorize("@ss.hasPermission('tms:transfer-item:import')")
//    public CommonResult
//        <Boolean> importTransferItemExcel(@RequestParam("file") MultipartFile file) throws Exception {
//        List<TmsTransferItemSaveReqVO> list = ExcelUtils.read(file, TmsTransferItemSaveReqVO.class);
//        // 可根据业务需要批量保存或校验
//        return success(true);
//    }
//
//
//}