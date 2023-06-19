package cn.iocoder.yudao.module.promotion.controller.app.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.banner.vo.AppBannerRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/promotion/banner")
@Tag(name = "用户 APP - 首页 Banner")
@Validated
public class AppBannerController {

    @GetMapping("/list")
    @Operation(summary = "获得 banner 列表")
    // todo @芋艿：swagger 注解，待补全
    // TODO @芋艿：可以增加缓存，提升性能
    // TODO @芋艿：position = 1 时，首页；position = 10 时，拼团活动页
    public CommonResult<List<AppBannerRespVO>> getBannerList(@RequestParam("position") Integer position) {
        List<AppBannerRespVO> bannerList = new ArrayList<>();
        AppBannerRespVO banner1 = new AppBannerRespVO();
        banner1.setUrl("https://www.example.com/link1");
        banner1.setPicUrl("https://api.java.crmeb.net/crmebimage/public/content/2022/08/04/0f78716213f64bfa83f191d51a832cbf73f6axavoy.jpg");
        bannerList.add(banner1);
        AppBannerRespVO banner2 = new AppBannerRespVO();
        banner2.setUrl("https://www.example.com/link2");
        banner2.setPicUrl("https://api.java.crmeb.net/crmebimage/public/content/2023/01/11/be09e755268b43ee90b0db3a3e1b7132r7a6t2wvsm.jpg");
        bannerList.add(banner2);
        return success(bannerList);
    }

}
