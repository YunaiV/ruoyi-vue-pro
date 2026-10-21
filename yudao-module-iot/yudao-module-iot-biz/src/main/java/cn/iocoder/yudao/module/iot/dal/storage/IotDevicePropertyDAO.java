package cn.iocoder.yudao.module.iot.dal.storage;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;

import java.util.List;
import java.util.Map;

// TODO DONE @AI：storage 包下，是不是加个 package-info.java，说明这个包的作用？已新增 package-info.java
/**
 * IoT 设备【属性】历史数据的存储 DAO 接口
 *
 * 由 yudao.iot.storage.type 选择 TDengine、IoTDB 其中一个实现。
 * 厂商相关的字段映射、DDL、字段差异、缺表判断等，都在实现类中处理。
 *
 * @author 芋道源码
 */
public interface IotDevicePropertyDAO {

    /**
     * 初始化：系统启动时，校验存储后端的前提条件
     */
    void initialize();

    /**
     * 定义（创建或修改）产品的属性表结构
     *
     * @param productId           产品编号
     * @param propertyThingModels 产品的属性类物模型列表
     */
    void defineProductPropertyTable(Long productId, List<IotThingModelDO> propertyThingModels);

    /**
     * 写入设备属性。只记录本次上报的属性
     *
     * @param device     设备
     * @param properties 属性，key 为物模型 identifier，value 为 Service 转换后的值
     * @param ts         存储时间（毫秒）
     * @param reportTime 上报时间（毫秒）
     */
    void insert(IotDeviceDO device, Map<String, Object> properties, Long ts, Long reportTime);

    /**
     * 查询设备属性的历史数据，按存储时间倒序
     *
     * 调用前，Service 已经校验：设备存在（包括逻辑删除）、属性标识符合法、时间范围完整
     *
     * @param productId 产品编号
     * @param reqVO     查询条件
     * @return 历史数据；属性表不存在时，返回空列表
     */
    List<IotDevicePropertyRespVO> selectHistoryList(Long productId, IotDevicePropertyHistoryListReqVO reqVO);

}
