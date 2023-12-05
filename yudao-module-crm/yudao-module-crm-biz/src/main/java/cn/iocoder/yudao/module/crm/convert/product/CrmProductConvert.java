package cn.iocoder.yudao.module.crm.convert.product;

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
        List<CrmProductRespVO> voList = BeanUtils.toBean(list, CrmProductRespVO.class);
        Map<Long, CrmProductCategoryDO> categoryMap = convertMap(categoryList, CrmProductCategoryDO::getId);
        for (CrmProductRespVO vo : voList) {
            MapUtils.findAndThen(categoryMap, vo.getCategoryId(), category -> vo.setCategoryName(category.getName()));
            MapUtils.findAndThen(userMap, vo.getOwnerUserId(), user -> vo.setOwnerUserName(user.getNickname()));
            MapUtils.findAndThen(userMap, Long.valueOf(vo.getCreator()), user -> vo.setCreatorName(user.getNickname()));
        }
        return voList;
    }

}
