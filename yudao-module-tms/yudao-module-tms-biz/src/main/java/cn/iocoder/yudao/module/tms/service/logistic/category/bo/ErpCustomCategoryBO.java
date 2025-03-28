package cn.iocoder.yudao.module.tms.service.logistic.category.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import lombok.Data;


@Data
public class ErpCustomCategoryBO extends ErpCustomCategoryDO {

    //对应产品数量
    private Long productCount;
}
