package cn.iocoder.yudao.module.product.convert.category;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.category.CategoryDO;

/**
 * 商品分类 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CategoryConvert {

    CategoryConvert INSTANCE = Mappers.getMapper(CategoryConvert.class);

    CategoryDO convert(CategoryCreateReqVO bean);

    CategoryDO convert(CategoryUpdateReqVO bean);

    CategoryRespVO convert(CategoryDO bean);

    List<CategoryRespVO> convertList(List<CategoryDO> list);

    PageResult<CategoryRespVO> convertPage(PageResult<CategoryDO> page);

    List<CategoryExcelVO> convertList02(List<CategoryDO> list);

}
