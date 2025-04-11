package cn.iocoder.yudao.module.tms.convert.logistic.category.item;

import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.TmsCustomCategoryItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.TmsCustomCategoryItemDO;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 海关分类子表 转换器
 */
@Mapper
public interface TmsCustomCategoryItemConvert {
    //instance
    TmsCustomCategoryItemConvert INSTANCE = org.mapstruct.factory.Mappers.getMapper(TmsCustomCategoryItemConvert.class);

    //VO->DO(不含customCategoryId，按需添加)
    TmsCustomCategoryItemDO convert(TmsCustomCategoryItemSaveReqVO bean);

    //listVO->listDO
    default List<TmsCustomCategoryItemDO> convert(List<TmsCustomCategoryItemSaveReqVO> list) {
        if (list == null) {
            return null;
        }
        List<TmsCustomCategoryItemDO> result = new ArrayList<>(list.size());
        for (TmsCustomCategoryItemSaveReqVO item : list) {
            result.add(convert(item));
        }
        return result;
    }
}
