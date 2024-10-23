package cn.iocoder.yudao.module.iot.service.tdengine;


import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;

import java.util.List;

/**
 * 数据结构接口
 */
public interface IotDbStructureDataService {

    /**
     * 创建物模型定义
     */
    void createSuperTable(ThingModelRespVO thingModel, Integer deviceType);

    /**
     * 更新物模型定义
     */
    void updateSuperTable(ThingModelRespVO thingModel, Integer deviceType);

    /**
     * 创建超级表数据模型
     */
    void createSuperTableDataModel(IotProductDO product, List<IotThinkModelFunctionDO> functionList);
}
