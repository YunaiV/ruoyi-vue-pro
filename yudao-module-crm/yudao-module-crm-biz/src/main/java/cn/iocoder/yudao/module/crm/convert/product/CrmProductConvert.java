package cn.iocoder.yudao.module.crm.convert.product;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 产品 Convert
 *
 * @author ZanGe丶
 */
@Mapper
public interface CrmProductConvert {

    CrmProductConvert INSTANCE = Mappers.getMapper(CrmProductConvert.class);

    default List<CrmProductRespVO> convertList(List<CrmProductDO> list,
                                               Map<Long, AdminUserRespDTO> userMap,
                                               List<CrmProductCategoryDO> categoryList) {
        Map<Long, CrmProductCategoryDO> categoryMap = convertMap(categoryList, CrmProductCategoryDO::getId);
        return CollectionUtils.convertList(list,
                product -> convert(product, userMap, categoryMap.get(product.getCategoryId())));
    }

    default CrmProductRespVO convert(CrmProductDO product,
                                     Map<Long, AdminUserRespDTO> userMap, CrmProductCategoryDO category) {
        CrmProductRespVO productVO = BeanUtils.toBean(product, CrmProductRespVO.class);
        Optional.ofNullable(category).ifPresent(c -> productVO.setCategoryName(c.getName()));
        MapUtils.findAndThen(userMap, productVO.getOwnerUserId(), user -> productVO.setOwnerUserName(user.getNickname()));
        MapUtils.findAndThen(userMap, Long.valueOf(productVO.getCreator()), user -> productVO.setCreatorName(user.getNickname()));
        return productVO;
    }

}
