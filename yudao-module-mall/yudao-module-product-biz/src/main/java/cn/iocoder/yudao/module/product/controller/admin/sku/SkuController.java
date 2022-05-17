package cn.iocoder.yudao.module.product.controller.admin.sku;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.SkuDO;
import cn.iocoder.yudao.module.product.convert.sku.SkuConvert;
import cn.iocoder.yudao.module.product.service.sku.SkuService;

@Api(tags = "管理后台 - 商品sku")
@RestController
@RequestMapping("/product/sku")
@Validated
public class SkuController {

    @Resource
    private SkuService skuService;

    @PostMapping("/create")
    @ApiOperation("创建商品sku")
    @PreAuthorize("@ss.hasPermission('product:sku:create')")
    public CommonResult<Integer> createSku(@Valid @RequestBody SkuCreateReqVO createReqVO) {
        return success(skuService.createSku(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新商品sku")
    @PreAuthorize("@ss.hasPermission('product:sku:update')")
    public CommonResult<Boolean> updateSku(@Valid @RequestBody SkuUpdateReqVO updateReqVO) {
        skuService.updateSku(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除商品sku")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:sku:delete')")
    public CommonResult<Boolean> deleteSku(@RequestParam("id") Integer id) {
        skuService.deleteSku(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得商品sku")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:sku:query')")
    public CommonResult<SkuRespVO> getSku(@RequestParam("id") Integer id) {
        SkuDO sku = skuService.getSku(id);
        return success(SkuConvert.INSTANCE.convert(sku));
    }

    @GetMapping("/list")
    @ApiOperation("获得商品sku列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:sku:query')")
    public CommonResult<List<SkuRespVO>> getSkuList(@RequestParam("ids") Collection<Integer> ids) {
        List<SkuDO> list = skuService.getSkuList(ids);
        return success(SkuConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得商品sku分页")
    @PreAuthorize("@ss.hasPermission('product:sku:query')")
    public CommonResult<PageResult<SkuRespVO>> getSkuPage(@Valid SkuPageReqVO pageVO) {
        PageResult<SkuDO> pageResult = skuService.getSkuPage(pageVO);
        return success(SkuConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出商品sku Excel")
    @PreAuthorize("@ss.hasPermission('product:sku:export')")
    @OperateLog(type = EXPORT)
    public void exportSkuExcel(@Valid SkuExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SkuDO> list = skuService.getSkuList(exportReqVO);
        // 导出 Excel
        List<SkuExcelVO> datas = SkuConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商品sku.xls", "数据", SkuExcelVO.class, datas);
    }

}
