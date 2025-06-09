package cn.iocoder.yudao.module.wms.controller.admin.stock.logic;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicExcelVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.vo.WmsStockLogicRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import cn.iocoder.yudao.module.wms.service.stock.logic.WmsStockLogicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "逻辑库存")
@RestController
@RequestMapping("/wms/stock-logic")
@Validated
public class WmsStockLogicController {

    @Resource
    private WmsStockLogicService stockLogicService;

    // /**
    // * @sign : 0E0BDA21A9064C48
    // */
    // @PostMapping("/create")
    // @Operation(summary = "创建逻辑库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic:create')")
    // public CommonResult<Long> createStockLogic(@Valid @RequestBody WmsStockLogicSaveReqVO createReqVO) {
    // return success(stockLogicService.createStockLogic(createReqVO).getId());
    // }
    // /**
    // * @sign : A40903BCE776025E
    // */
    // @PutMapping("/update")
    // @Operation(summary = "更新逻辑库存")
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic:update')")
    // public CommonResult<Boolean> updateStockLogic(@Valid @RequestBody WmsStockLogicSaveReqVO updateReqVO) {
    // stockLogicService.updateStockLogic(updateReqVO);
    // return success(true);
    // }
    // @DeleteMapping("/delete")
    // @Operation(summary = "删除逻辑库存")
    // @Parameter(name = "id", description = "编号", required = true)
    // @PreAuthorize("@ss.hasPermission('wms:stock-logic:delete')")
    // public CommonResult<Boolean> deleteStockLogic(@RequestParam("id") Long id) {
    // stockLogicService.deleteStockLogic(id);
    // return success(true);
    // }

    /**
     * @sign : 7CDB60ED7A6D3E5E
     */
    @GetMapping("/stocks")
    @Operation(summary = "获得产品的逻辑库存清单")
    @Parameter(name = "warehouseId", description = "仓库ID", required = true, example = "1024")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic:query')")
    public CommonResult<List<WmsStockLogicRespVO>> selectStockLogicList(@RequestParam("warehouseId") Long warehouseId, @RequestParam("productId") Long productId) {
        // 查询数据
        List<WmsStockLogicDO> stockLogicList = stockLogicService.selectStockLogic(warehouseId, productId, null, null);
        // 转换
        List<WmsStockLogicRespVO> stockLogicVOList = BeanUtils.toBean(stockLogicList, WmsStockLogicRespVO.class);
        // 返回
        return success(stockLogicVOList);
    }

    /**
     * @sign : 7CDB60ED7A6D3E5E
     */
    @GetMapping("/stock")
    @Operation(summary = "获得产品的逻辑库存")
    @Parameter(name = "warehouseId", description = "仓库ID", required = true, example = "1024")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    @Parameter(name = "companyId", description = "公司ID", required = true, example = "1024")
    @Parameter(name = "deptId", description = "部门ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic:query')")
    public CommonResult<WmsStockLogicRespVO> selectStockLogic(@RequestParam("warehouseId") Long warehouseId, @RequestParam("productId") Long productId, @RequestParam("companyId") Long companyId, @RequestParam("deptId") Long deptId) {
        // 查询数据
        List<WmsStockLogicDO> stockLogicList = stockLogicService.selectStockLogic(warehouseId, productId, companyId, deptId);
        // 转换
        List<WmsStockLogicRespVO> stockLogicVOList = BeanUtils.toBean(stockLogicList, WmsStockLogicRespVO.class);
        if (CollectionUtils.isEmpty(stockLogicVOList)) {
            return success(null);
        } else {
            // 返回
            return success(stockLogicVOList.get(0));
        }
    }

    /**
     * @sign : EC951F0579860D97
     */
    @PostMapping("/page")
    @Operation(summary = "获得逻辑库存分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic:query')")
    public CommonResult<PageResult<WmsStockLogicRespVO>> getStockLogicPage(@Valid @RequestBody WmsStockLogicPageReqVO pageReqVO) {
        // 查询数据
        PageResult<WmsStockLogicDO> doPageResult = stockLogicService.getStockLogicPage(pageReqVO);
        // 转换
        PageResult<WmsStockLogicRespVO> voPageResult = BeanUtils.toBean(doPageResult, WmsStockLogicRespVO.class);
        // 装配模型
        stockLogicService.assembleProducts(voPageResult.getList());
        stockLogicService.assembleWarehouse(voPageResult.getList());
        stockLogicService.assembleDept(voPageResult.getList());
        stockLogicService.assembleCompany(voPageResult.getList());
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
            .mapping(WmsStockLogicRespVO::getCreator, WmsStockLogicRespVO::setCreatorName)
            .mapping(WmsStockLogicRespVO::getUpdater, WmsStockLogicRespVO::setUpdaterName)
            .fill();
        // 返回
        return success(voPageResult);
    }

    @PostMapping("/export-excel")
    @Operation(summary = "导出逻辑库存 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-logic:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockLogicExcel(@Valid @RequestBody WmsStockLogicPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockLogicRespVO> voList = this.getStockLogicPage(pageReqVO).getData().getList();
        List<WmsStockLogicExcelVO> xlsList = BeanUtils.toBean(voList, WmsStockLogicExcelVO.class);
        Map<Long, WmsStockLogicRespVO> voMap = StreamX.from(voList).toMap(WmsStockLogicRespVO::getId);
        for (WmsStockLogicExcelVO excelVO : xlsList) {
            WmsStockLogicRespVO vo = voMap.get(excelVO.getId());
            if (vo != null) {
                excelVO.setWarehouseName(vo.getWarehouse().getName());
                excelVO.setProductCode(vo.getProduct().getCode());
                excelVO.setCompanyName(vo.getCompany().getName());
                excelVO.setDeptName(vo.getDept().getName());
            }
        }
        // 导出 Excel
        ExcelUtils.write(response, "逻辑库存.xls", "数据", WmsStockLogicExcelVO.class, xlsList);
    }
}
