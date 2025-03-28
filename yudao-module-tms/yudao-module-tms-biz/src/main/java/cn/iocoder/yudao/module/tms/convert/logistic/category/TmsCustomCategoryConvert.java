package cn.iocoder.yudao.module.tms.convert.logistic.category;

import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 海关分类 转换器
 */
@Mapper
public interface TmsCustomCategoryConvert {
    //instance
    TmsCustomCategoryConvert INSTANCE = org.mapstruct.factory.Mappers.getMapper(TmsCustomCategoryConvert.class);

    //VO->DO
    TmsCustomCategoryDO convert(TmsCustomCategorySaveReqVO bean);

    default List<TmsCustomCategoryDO> convert(List<TmsCustomCategorySaveReqVO> list) {
        if (list == null) {
            return null;
        }
        List<TmsCustomCategoryDO> result = new ArrayList<>(list.size());
        for (TmsCustomCategorySaveReqVO bean : list) {
            result.add(convert(bean));
        }
        return result;
    }
}
