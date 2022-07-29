package cn.iocoder.yudao.module.product.controller.app.category;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.app.category.vo.AppCategoryListRespVO;
import cn.iocoder.yudao.module.product.convert.category.CategoryConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.product.service.category.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户 APP - 商品分类")
@RestController
@RequestMapping("/product/category")
@Validated
public class AppCategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("/list")
    @ApiOperation("获得商品分类列表")
    public CommonResult<List<AppCategoryListRespVO>> listByQuery() {
        List<CategoryDO> list = categoryService.getCategoryList();
        list.sort(Comparator.comparing(CategoryDO::getSort));
        return success(CategoryConvert.INSTANCE.convertList03(list));
    }



}
