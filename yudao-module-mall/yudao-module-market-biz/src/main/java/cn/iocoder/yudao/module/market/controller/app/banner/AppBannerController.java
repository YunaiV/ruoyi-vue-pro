package cn.iocoder.yudao.module.market.controller.app.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.market.controller.admin.banner.vo.BannerRespVO;
import cn.iocoder.yudao.module.market.convert.banner.BannerConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.banner.BannerDO;
import cn.iocoder.yudao.module.market.service.banner.BannerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author: XIA
 */
@RestController
@RequestMapping("/market/banner")
@Api(tags = "用户APP- 首页Banner")
@Validated
public class AppBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/list")
    @ApiOperation("获得banner列表")
    @PreAuthorize("@ss.hasPermission('market:banner:query')")
    public CommonResult<List<BannerRespVO>> getBannerList() {
        List<BannerDO> list = bannerService.getBannerList();
        return success(BannerConvert.INSTANCE.convertList(list));
    }

}
