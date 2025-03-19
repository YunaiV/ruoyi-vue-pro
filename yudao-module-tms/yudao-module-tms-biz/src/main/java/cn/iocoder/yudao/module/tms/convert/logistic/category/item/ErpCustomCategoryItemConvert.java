package cn.iocoder.yudao.module.tms.convert.logistic.category.item;

import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 海关分类子表 转换器
 */
@Mapper
public interface ErpCustomCategoryItemConvert {
    //instance
    ErpCustomCategoryItemConvert INSTANCE = org.mapstruct.factory.Mappers.getMapper(ErpCustomCategoryItemConvert.class);

    //VO->DO(不含customCategoryId，按需添加)
    ErpCustomCategoryItemDO convert(ErpCustomCategoryItemSaveReqVO bean);

    //listVO->listDO
    default List<ErpCustomCategoryItemDO> convert(List<ErpCustomCategoryItemSaveReqVO> list) {
        if (list == null) {
            return null;
        }
        List<ErpCustomCategoryItemDO> result = new ArrayList<>(list.size());
        for (ErpCustomCategoryItemSaveReqVO item : list) {
            result.add(convert(item));
        }
        return result;
    }
}
