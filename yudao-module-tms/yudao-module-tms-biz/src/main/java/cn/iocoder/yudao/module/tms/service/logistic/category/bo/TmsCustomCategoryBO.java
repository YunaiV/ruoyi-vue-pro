package cn.iocoder.yudao.module.tms.service.logistic.category.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import lombok.Data;


@Data
public class TmsCustomCategoryBO extends TmsCustomCategoryDO {

    //对应产品数量
    private Long productCount;
}
