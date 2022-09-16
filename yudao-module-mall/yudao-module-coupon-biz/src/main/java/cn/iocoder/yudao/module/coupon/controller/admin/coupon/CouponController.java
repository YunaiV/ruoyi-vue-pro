package cn.iocoder.yudao.module.coupon.controller.admin.coupon;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.*;

import javax.validation.*;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;


import cn.iocoder.yudao.module.coupon.controller.admin.coupon.vo.*;
import cn.iocoder.yudao.module.coupon.dal.dataobject.coupon.CouponDO;
import cn.iocoder.yudao.module.coupon.convert.coupon.CouponConvert;
import cn.iocoder.yudao.module.coupon.service.coupon.CouponService;

@Api(tags = "管理后台 - 优惠券")
@RestController
@RequestMapping("/coupon/item")
@Validated
public class CouponController {

    @Resource
    private CouponService couponService;


    //todo 用户优惠券
    @PostMapping("/create")
    @ApiOperation("用户领取优惠券")
    @PreAuthorize("@ss.hasPermission('coupon::create')")
    public CommonResult<Long> create(@RequestParam("couponTemplateId") Long couponTemplateId) {

        return success(couponService.create(couponTemplateId));
    }


    @PutMapping("/update")
    @ApiOperation("更新优惠券")
    @PreAuthorize("@ss.hasPermission('coupon::update')")
    public CommonResult<Boolean> update(@Valid @RequestBody CouponUpdateReqVO updateReqVO) {
        couponService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除优惠券")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('coupon::delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        couponService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得优惠券")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('coupon::query')")
    public CommonResult<CouponRespVO> get(@RequestParam("id") Long id) {
        CouponDO couponDO = couponService.get(id);
        return success(CouponConvert.INSTANCE.convert(couponDO));
    }



    @GetMapping("/list")
    @ApiOperation("获得优惠券列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('coupon::query')")
    public CommonResult<List<CouponRespVO>> getList(@RequestParam("ids") Collection<Long> ids) {
        List<CouponDO> list = couponService.getList(ids);
        return success(CouponConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得优惠券分页")
    @PreAuthorize("@ss.hasPermission('coupon::query')")
    public CommonResult<PageResult<CouponRespVO>> getPage(@Valid CouponPageReqVO pageVO) {
        PageResult<CouponDO> pageResult = couponService.getPage(pageVO);
        return success(CouponConvert.INSTANCE.convertPage(pageResult));
    }


}
