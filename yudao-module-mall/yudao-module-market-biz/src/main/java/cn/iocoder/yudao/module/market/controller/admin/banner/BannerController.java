package cn.iocoder.yudao.module.market.controller.admin.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.banner.vo.*;
import cn.iocoder.yudao.module.market.convert.banner.BannerConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.banner.BannerDO;
import cn.iocoder.yudao.module.market.service.banner.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - Banner 管理")
@RestController
@RequestMapping("/market/banner")
@Validated
public class BannerController {

    @Resource
    private BannerService bannerService;

    @PostMapping("/create")
    @ApiOperation("创建 Banner")
    @PreAuthorize("@ss.hasPermission('market:banner:create')")
    public CommonResult<Long> createBanner(@Valid @RequestBody BannerCreateReqVO createReqVO) {
        return success(bannerService.createBanner(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新 Banner")
    @PreAuthorize("@ss.hasPermission('market:banner:update')")
    public CommonResult<Boolean> updateBanner(@Valid @RequestBody BannerUpdateReqVO updateReqVO) {
        bannerService.updateBanner(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除 Banner")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('market:banner:delete')")
    public CommonResult<Boolean> deleteBanner(@RequestParam("id") Long id) {
        bannerService.deleteBanner(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得 Banner")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('market:banner:query')")
    public CommonResult<BannerRespVO> getBanner(@RequestParam("id") Long id) {
        BannerDO banner = bannerService.getBanner(id);
        return success(BannerConvert.INSTANCE.convert(banner));
    }

    @GetMapping("/page")
    @ApiOperation("获得 Banner 分页")
    @PreAuthorize("@ss.hasPermission('market:banner:query')")
    public CommonResult<PageResult<BannerRespVO>> getBannerPage(@Valid BannerPageReqVO pageVO) {
        PageResult<BannerDO> pageResult = bannerService.getBannerPage(pageVO);
        return success(BannerConvert.INSTANCE.convertPage(pageResult));
    }

}
