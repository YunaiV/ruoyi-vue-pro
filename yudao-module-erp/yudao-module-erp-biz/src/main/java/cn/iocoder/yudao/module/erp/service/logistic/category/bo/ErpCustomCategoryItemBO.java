package cn.iocoder.yudao.module.erp.service.logistic.category.bo;

import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import lombok.Data;

//1子表 : 1主表 : 1产品
@Data
public class ErpCustomCategoryItemBO extends ErpCustomCategoryItemDO {
    private ErpProductDO erpProductDO;
    private ErpCustomCategoryDO erpCustomCategoryDO;
}
