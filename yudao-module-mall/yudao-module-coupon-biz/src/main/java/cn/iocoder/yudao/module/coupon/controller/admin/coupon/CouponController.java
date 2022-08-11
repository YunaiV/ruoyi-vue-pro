package cn.iocoder.yudao.module.coupon.controller.admin.coupon;

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

import cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo.*;
import cn.iocoder.yudao.module.coupon.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.coupon.convert.coupon.CouponConvert;
import cn.iocoder.yudao.module.coupon.service.coupon.CouponService;

@Api(tags = "管理后台 - 优惠券")
@RestController
@RequestMapping("/coupon/")
@Validated
public class CouponController {

    @Resource
    private CouponService Service;

    @PostMapping("/create")
    @ApiOperation("创建优惠券")
    @PreAuthorize("@ss.hasPermission('coupon::create')")
    public CommonResult<Long> create(@Valid @RequestBody CouponCreateReqVO createReqVO) {
        return success(Service.create(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新优惠券")
    @PreAuthorize("@ss.hasPermission('coupon::update')")
    public CommonResult<Boolean> update(@Valid @RequestBody CouponUpdateReqVO updateReqVO) {
        Service.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除优惠券")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('coupon::delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        Service.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得优惠券")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('coupon::query')")
    public CommonResult<CouponRespVO> get(@RequestParam("id") Long id) {
        CouponDO couponDO = Service.get(id);
        return success(CouponConvert.INSTANCE.convert(couponDO));
    }

    @GetMapping("/list")
    @ApiOperation("获得优惠券列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('coupon::query')")
    public CommonResult<List<CouponRespVO>> getList(@RequestParam("ids") Collection<Long> ids) {
        List<CouponDO> list = Service.getList(ids);
        return success(CouponConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得优惠券分页")
    @PreAuthorize("@ss.hasPermission('coupon::query')")
    public CommonResult<PageResult<CouponRespVO>> getPage(@Valid CouponPageReqVO pageVO) {
        PageResult<CouponDO> pageResult = Service.getPage(pageVO);
        return success(CouponConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出优惠券 Excel")
    @PreAuthorize("@ss.hasPermission('coupon::export')")
    @OperateLog(type = EXPORT)
    public void exportExcel(@Valid CouponExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<CouponDO> list = Service.getList(exportReqVO);
        // 导出 Excel
        List<CouponExcelVO> datas = CouponConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "优惠券.xls", "数据", CouponExcelVO.class, datas);
    }

}
