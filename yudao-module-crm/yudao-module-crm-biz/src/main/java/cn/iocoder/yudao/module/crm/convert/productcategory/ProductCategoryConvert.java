package cn.iocoder.yudao.module.crm.convert.productcategory;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.productcategory.ProductCategoryDO;

/**
 * 产品分类 Convert
 *
 * @author ZanGe丶
 */
@Mapper
public interface ProductCategoryConvert {

    ProductCategoryConvert INSTANCE = Mappers.getMapper(ProductCategoryConvert.class);

    ProductCategoryDO convert(ProductCategoryCreateReqVO bean);

    ProductCategoryDO convert(ProductCategoryUpdateReqVO bean);

    ProductCategoryRespVO convert(ProductCategoryDO bean);

    List<ProductCategoryRespVO> convertList(List<ProductCategoryDO> list);

}
