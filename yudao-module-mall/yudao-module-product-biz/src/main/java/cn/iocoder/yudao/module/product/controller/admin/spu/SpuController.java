package cn.iocoder.yudao.module.product.controller.admin.spu;

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

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.SpuDO;
import cn.iocoder.yudao.module.product.convert.spu.SpuConvert;
import cn.iocoder.yudao.module.product.service.spu.SpuService;

@Api(tags = "管理后台 - 商品spu")
@RestController
@RequestMapping("/product/spu")
@Validated
public class SpuController {

    @Resource
    private SpuService spuService;

    @PostMapping("/create")
    @ApiOperation("创建商品spu")
    @PreAuthorize("@ss.hasPermission('product:spu:create')")
    public CommonResult<Integer> createSpu(@Valid @RequestBody SpuCreateReqVO createReqVO) {
        return success(spuService.createSpu(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新商品spu")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> updateSpu(@Valid @RequestBody SpuUpdateReqVO updateReqVO) {
        spuService.updateSpu(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除商品spu")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:spu:delete')")
    public CommonResult<Boolean> deleteSpu(@RequestParam("id") Integer id) {
        spuService.deleteSpu(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得商品spu")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Integer.class)
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<SpuRespVO> getSpu(@RequestParam("id") Integer id) {
        SpuDO spu = spuService.getSpu(id);
        return success(SpuConvert.INSTANCE.convert(spu));
    }

    @GetMapping("/list")
    @ApiOperation("获得商品spu列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<SpuRespVO>> getSpuList(@RequestParam("ids") Collection<Integer> ids) {
        List<SpuDO> list = spuService.getSpuList(ids);
        return success(SpuConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得商品spu分页")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<PageResult<SpuRespVO>> getSpuPage(@Valid SpuPageReqVO pageVO) {
        PageResult<SpuDO> pageResult = spuService.getSpuPage(pageVO);
        return success(SpuConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出商品spu Excel")
    @PreAuthorize("@ss.hasPermission('product:spu:export')")
    @OperateLog(type = EXPORT)
    public void exportSpuExcel(@Valid SpuExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SpuDO> list = spuService.getSpuList(exportReqVO);
        // 导出 Excel
        List<SpuExcelVO> datas = SpuConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "商品spu.xls", "数据", SpuExcelVO.class, datas);
    }

}
