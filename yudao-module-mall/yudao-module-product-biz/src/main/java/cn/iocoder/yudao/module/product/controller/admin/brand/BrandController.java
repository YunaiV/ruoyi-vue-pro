package cn.iocoder.yudao.module.product.controller.admin.brand;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.*;
import cn.iocoder.yudao.module.product.convert.brand.BrandConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.BrandDO;
import cn.iocoder.yudao.module.product.service.brand.BrandService;
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
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "管理后台 - 品牌")
@RestController
@RequestMapping("/product/brand")
@Validated
public class BrandController {

    @Resource
    private BrandService brandService;

    @PostMapping("/create")
    @ApiOperation("创建品牌")
    @PreAuthorize("@ss.hasPermission('product:brand:create')")
    public CommonResult<Long> createBrand(@Valid @RequestBody BrandCreateReqVO createReqVO) {
        return success(brandService.createBrand(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新品牌")
    @PreAuthorize("@ss.hasPermission('product:brand:update')")
    public CommonResult<Boolean> updateBrand(@Valid @RequestBody BrandUpdateReqVO updateReqVO) {
        brandService.updateBrand(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除品牌")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:brand:delete')")
    public CommonResult<Boolean> deleteBrand(@RequestParam("id") Long id) {
        brandService.deleteBrand(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得品牌")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('product:brand:query')")
    public CommonResult<BrandRespVO> getBrand(@RequestParam("id") Long id) {
        BrandDO brand = brandService.getBrand(id);
        return success(BrandConvert.INSTANCE.convert(brand));
    }

    @GetMapping("/page")
    @ApiOperation("获得品牌分页")
    @PreAuthorize("@ss.hasPermission('product:brand:query')")
    public CommonResult<PageResult<BrandRespVO>> getBrandPage(@Valid BrandPageReqVO pageVO) {
        PageResult<BrandDO> pageResult = brandService.getBrandPage(pageVO);
        return success(BrandConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出品牌 Excel")
    @PreAuthorize("@ss.hasPermission('product:brand:export')")
    @OperateLog(type = EXPORT)
    public void exportBrandExcel(@Valid BrandExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<BrandDO> list = brandService.getBrandList(exportReqVO);
        // 导出 Excel
        List<BrandExcelVO> datas = BrandConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "品牌.xls", "数据", BrandExcelVO.class, datas);
    }

}
