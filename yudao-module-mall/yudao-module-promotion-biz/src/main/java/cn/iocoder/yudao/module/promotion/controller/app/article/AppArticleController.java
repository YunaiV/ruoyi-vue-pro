package cn.iocoder.yudao.module.promotion.controller.app.article;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.app.article.vo.article.AppArticlePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.article.vo.article.AppArticleRespVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 文章")
@RestController
@RequestMapping("/promotion/article")
@Validated
public class AppArticleController {

    @RequestMapping("/list")
    // TODO @芋艿：swagger 注解
    public CommonResult<List<AppArticleRespVO>> getArticleList(@RequestParam(value = "recommendHot", required = false) Boolean recommendHot,
                                                               @RequestParam(value = "recommendBanner", required = false) Boolean recommendBanner) {
        List<AppArticleRespVO> appArticleRespVOList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppArticleRespVO appArticleRespVO = new AppArticleRespVO();
            appArticleRespVO.setId((long) (i + 1));
            appArticleRespVO.setTitle("芋道源码 - " + i + "模块");
            appArticleRespVO.setAuthor("芋道源码");
            appArticleRespVO.setCategoryId((long) random.nextInt(10000));
            appArticleRespVO.setPicUrl("https://www.iocoder.cn/" + (i + 1) + ".png");
            appArticleRespVO.setIntroduction("我是简介");
            appArticleRespVO.setDescription("我是详细");
            appArticleRespVO.setCreateTime(LocalDateTime.now());
            appArticleRespVO.setBrowseCount(random.nextInt(10000));
            appArticleRespVO.setSpuId((long) random.nextInt(10000));
            appArticleRespVOList.add(appArticleRespVO);
        }
        return success(appArticleRespVOList);
    }

    @RequestMapping("/page")
    // TODO @芋艿：swagger 注解
    public CommonResult<PageResult<AppArticleRespVO>> getArticlePage(AppArticlePageReqVO pageReqVO) {
        List<AppArticleRespVO> appArticleRespVOList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppArticleRespVO appArticleRespVO = new AppArticleRespVO();
            appArticleRespVO.setId((long) (i + 1));
            appArticleRespVO.setTitle("芋道源码 - " + i + "模块");
            appArticleRespVO.setAuthor("芋道源码");
            appArticleRespVO.setCategoryId((long) random.nextInt(10000));
            appArticleRespVO.setPicUrl("https://www.iocoder.cn/" + (i + 1) + ".png");
            appArticleRespVO.setIntroduction("我是简介");
            appArticleRespVO.setDescription("我是详细");
            appArticleRespVO.setCreateTime(LocalDateTime.now());
            appArticleRespVO.setBrowseCount(random.nextInt(10000));
            appArticleRespVO.setSpuId((long) random.nextInt(10000));
            appArticleRespVOList.add(appArticleRespVO);
        }
        return success(new PageResult<>(appArticleRespVOList, 10L));
    }

    @RequestMapping("/get")
    // TODO @芋艿：swagger 注解
    public CommonResult<AppArticleRespVO> getArticlePage(@RequestParam("id") Long id) {
        Random random = new Random();
        AppArticleRespVO appArticleRespVO = new AppArticleRespVO();
        appArticleRespVO.setId((long) (1));
        appArticleRespVO.setTitle("芋道源码 - " + 0 + "模块");
        appArticleRespVO.setAuthor("芋道源码");
        appArticleRespVO.setCategoryId((long) random.nextInt(10000));
        appArticleRespVO.setPicUrl("https://www.iocoder.cn/" + (0 + 1) + ".png");
        appArticleRespVO.setIntroduction("我是简介");
        appArticleRespVO.setDescription("我是详细");
        appArticleRespVO.setCreateTime(LocalDateTime.now());
        appArticleRespVO.setBrowseCount(random.nextInt(10000));
        appArticleRespVO.setSpuId((long) random.nextInt(10000));
        appArticleRespVO.setSpuId(633L);
        return success(appArticleRespVO);
    }

}
