package cn.iocoder.yudao.module.product.controller.admin.spu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

/**
 * 商品 SPU 相关接口
 *
 * @author HUIHUI
 */
@Tag(name = "管理后台 - 商品 SPU")
@RestController
@RequestMapping("/product/spu")
@Validated
public class ProductSpuController {

    @Resource
    private ProductSpuService productSpuService;

    @PostMapping("/create")
    @Operation(summary = "创建商品 SPU")
    @PreAuthorize("@ss.hasPermission('product:spu:create')")
    public CommonResult<Long> createProductSpu(@Valid @RequestBody ProductSpuCreateReqVO createReqVO) {
        return success(productSpuService.createSpu(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品 SPU")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> updateSpu(@Valid @RequestBody ProductSpuUpdateReqVO updateReqVO) {
        productSpuService.updateSpu(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新商品 SPU Status")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody ProductSpuUpdateStatusReqVO updateReqVO) {
        productSpuService.updateStatus(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品 SPU")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:spu:delete')")
    public CommonResult<Boolean> deleteSpu(@RequestParam("id") Long id) {
        productSpuService.deleteSpu(id);
        return success(true);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得商品 SPU 明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<ProductSpuDetailRespVO> getSpuDetail(@RequestParam("id") Long id) {
        return success(productSpuService.getSpuDetail(id));
    }

    @GetMapping("/get-simple-list")
    @Operation(summary = "获得商品 SPU 精简列表")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<ProductSpuSimpleRespVO>> getSpuSimpleList() {
        List<ProductSpuDO> list = productSpuService.getSpuList();
        return success(ProductSpuConvert.INSTANCE.convertList02(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品 SPU 分页")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<PageResult<ProductSpuRespVO>> getSpuPage(@Valid ProductSpuPageReqVO pageVO) {
        return success(ProductSpuConvert.INSTANCE.convertPage(productSpuService.getSpuPage(pageVO)));
    }

    // TODO @tuihui999：get-count；另外，url 使用 - 拆分  fix
    @GetMapping("/get-count")
    @Operation(summary = "获得商品 SPU 分页 tab count")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<Map<Integer, Long>> getTabsCount() {
        return success(productSpuService.getTabsCount());
    }

    @GetMapping("/export")
    @Operation(summary = "导出用户")
    @PreAuthorize("@ss.hasPermission('product:spu:export')")
    @OperateLog(type = EXPORT)
    public void exportUserList(@Validated ProductSpuExportReqVO reqVO,
                               HttpServletResponse response) throws IOException {
        List<ProductSpuDO> spuList = productSpuService.getSpuList(reqVO);
        // 导出 Excel
        List<ProductSpuExcelVO> datas = ProductSpuConvert.INSTANCE.convertList03(spuList);
        ExcelUtils.write(response, "商品spu.xls", "数据", ProductSpuExcelVO.class, datas);
    }
}
