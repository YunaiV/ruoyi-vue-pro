package cn.iocoder.yudao.module.product.controller.admin.spu;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 商品spu")
@RestController
@RequestMapping("/product/spu")
@Validated
public class ProductSpuController {

    @Resource
    private ProductSpuService spuService;

    @PostMapping("/create")
    @ApiOperation("创建商品spu")
    @PreAuthorize("@ss.hasPermission('product:spu:create')")
    public CommonResult<Long> createSpu(@Valid @RequestBody ProductSpuCreateReqVO createReqVO) {
        return success(spuService.createSpu(createReqVO));
    }

    // TODO @franky：SpuUpdateReqVO 缺少前缀
    @PutMapping("/update")
    @ApiOperation("更新商品spu")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> updateSpu(@Valid @RequestBody SpuUpdateReqVO updateReqVO) {
        spuService.updateSpu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除商品spu")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:spu:delete')")
    public CommonResult<Boolean> deleteSpu(@RequestParam("id") Long id) {
        spuService.deleteSpu(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得商品spu")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<SpuRespVO> getSpu(@RequestParam("id") Long id) {
        return success(spuService.getSpu(id));
    }

    @GetMapping("/list")
    @ApiOperation("获得商品spu列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<SpuRespVO>> getSpuList(@RequestParam("ids") Collection<Long> ids) {
        List<ProductSpuDO> list = spuService.getSpuList(ids);
        return success(ProductSpuConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得商品spu分页")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<PageResult<SpuRespVO>> getSpuPage(@Valid SpuPageReqVO pageVO) {
        return success(spuService.getSpuPage(pageVO));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出商品spu Excel")
    @PreAuthorize("@ss.hasPermission('product:spu:export')")
    @OperateLog(type = EXPORT)
    public void exportSpuExcel(@Valid SpuExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ProductSpuDO> list = spuService.getSpuList(exportReqVO);
        // 导出 Excel
        List<SpuExcelVO> datas = ProductSpuConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商品spu.xls", "数据", SpuExcelVO.class, datas);
    }

}
