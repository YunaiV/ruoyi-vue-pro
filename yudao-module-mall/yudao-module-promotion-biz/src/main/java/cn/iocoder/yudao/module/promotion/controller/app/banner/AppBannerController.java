package cn.iocoder.yudao.module.promotion.controller.app.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.banner.vo.AppBannerRespVO;
import cn.iocoder.yudao.module.promotion.convert.banner.BannerConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.banner.BannerDO;
import cn.iocoder.yudao.module.promotion.service.banner.BannerService;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;

@RestController
@RequestMapping("/promotion/banner")
@Tag(name = "用户 APP - 首页 Banner")
@Validated
public class AppBannerController {

    // TODO @puhui999：这个目前不缓存，也没问题，因为首页没用到。
    /**
     * {@link AppBannerRespVO} 缓存，通过它异步刷新 {@link #getBannerList0(Integer)} 所要的首页数据
     */
    private final LoadingCache<Integer, List<AppBannerRespVO>> bannerListCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<Integer, List<AppBannerRespVO>>() {

                @Override
                public List<AppBannerRespVO> load(Integer position) {
                    return getBannerList0(position);
                }

            });

    @Resource
    private BannerService bannerService;

    @GetMapping("/list")
    @Operation(summary = "获得 banner 列表")
    @Parameter(name = "position", description = "Banner position", example = "1")
    public CommonResult<List<AppBannerRespVO>> getBannerList(@RequestParam("position") Integer position) {
        return success(bannerListCache.getUnchecked(position));
    }

    private List<AppBannerRespVO> getBannerList0(Integer position) {
        List<BannerDO> bannerList = bannerService.getBannerListByPosition(position);
        return BannerConvert.INSTANCE.convertList01(bannerList);
    }

    @PutMapping("/add-browse-count")
    @Operation(summary = "增加 Banner 点击量")
    @Parameter(name = "id", description = "Banner 编号", example = "1024")
    public CommonResult<Boolean> addBrowseCount(@RequestParam("id") Long id) {
        bannerService.addBannerBrowseCount(id);
        return success(true);
    }

}
