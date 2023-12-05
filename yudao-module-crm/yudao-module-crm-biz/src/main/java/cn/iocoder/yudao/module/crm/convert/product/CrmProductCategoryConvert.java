package cn.iocoder.yudao.module.crm.convert.product;

import cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory.CrmProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory.CrmProductCategoryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.productcategory.CrmProductCategoryUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 产品分类 Convert
 *
 * @author ZanGe丶
 */
@Mapper
public interface CrmProductCategoryConvert {

    CrmProductCategoryConvert INSTANCE = Mappers.getMapper(CrmProductCategoryConvert.class);

    CrmProductCategoryDO convert(CrmProductCategoryCreateReqVO bean);

    CrmProductCategoryDO convert(CrmProductCategoryUpdateReqVO bean);

    CrmProductCategoryRespVO convert(CrmProductCategoryDO bean);

    List<CrmProductCategoryRespVO> convertList(List<CrmProductCategoryDO> list);

}
