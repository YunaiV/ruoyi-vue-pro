package cn.iocoder.yudao.module.iot.service.tdengine;


import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotProductThingModelDO;

import java.util.List;

/**
 * IoT 超级表服务，负责根据物模型创建和更新超级表，以及创建超级表的子表等操作。
 */
public interface IotSuperTableService {

    /**
     * 创建超级表数据模型
     */
    void createSuperTableDataModel(IotProductDO product, List<IotProductThingModelDO> thingModelList);

}