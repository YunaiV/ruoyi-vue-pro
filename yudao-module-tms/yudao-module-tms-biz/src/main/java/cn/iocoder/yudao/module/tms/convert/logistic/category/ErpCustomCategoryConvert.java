package cn.iocoder.yudao.module.tms.convert.logistic.category;

import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 海关分类 转换器
 */
@Mapper
public interface ErpCustomCategoryConvert {
    //instance
    ErpCustomCategoryConvert INSTANCE = org.mapstruct.factory.Mappers.getMapper(ErpCustomCategoryConvert.class);

    //VO->DO
    ErpCustomCategoryDO convert(ErpCustomCategorySaveReqVO bean);

    default List<ErpCustomCategoryDO> convert(List<ErpCustomCategorySaveReqVO> list) {
        if (list == null) {
            return null;
        }
        List<ErpCustomCategoryDO> result = new ArrayList<>(list.size());
        for (ErpCustomCategorySaveReqVO bean : list) {
            result.add(convert(bean));
        }
        return result;
    }
}
