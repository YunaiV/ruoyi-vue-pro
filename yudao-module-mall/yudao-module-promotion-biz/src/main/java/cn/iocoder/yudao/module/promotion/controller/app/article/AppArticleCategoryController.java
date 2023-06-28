package cn.iocoder.yudao.module.promotion.controller.app.article;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.article.vo.category.AppArticleCategoryRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 文章分类")
@RestController
@RequestMapping("/promotion/article-category")
@Validated
public class AppArticleCategoryController {

    @RequestMapping("/list")
    @Operation(summary = "获得文章分类列表")
    // TODO @芋艿：swagger 注解
    public CommonResult<List<AppArticleCategoryRespVO>> getArticleCategoryList() {
        List<AppArticleCategoryRespVO> appArticleRespVOList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            AppArticleCategoryRespVO appArticleRespVO = new AppArticleCategoryRespVO();
            appArticleRespVO.setId((long) (i + 1));
            appArticleRespVO.setName("分类 - " + i);
            appArticleRespVO.setPicUrl("https://www.iocoder.cn/" + (i + 1) + ".png");
            appArticleRespVOList.add(appArticleRespVO);
        }
        return success(appArticleRespVOList);
    }

}
