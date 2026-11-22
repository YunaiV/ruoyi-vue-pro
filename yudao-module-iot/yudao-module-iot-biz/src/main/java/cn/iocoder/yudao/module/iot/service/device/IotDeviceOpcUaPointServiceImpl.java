package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointSaveReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.util.IotOpcUaUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceOpcUaPointMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 OPC UA 点位配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotDeviceOpcUaPointServiceImpl implements IotDeviceOpcUaPointService {

    @Resource
    private IotDeviceOpcUaPointMapper opcUaPointMapper;

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;
    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Long createDeviceOpcUaPoint(IotDeviceOpcUaPointSaveReqVO createReqVO) {
        // 1. 校验设备、产品协议、物模型属性、NodeId、唯一性
        validateDeviceOpcUaPoint(createReqVO, null);

        // 2. 插入
        IotDeviceOpcUaPointDO point = BeanUtils.toBean(createReqVO, IotDeviceOpcUaPointDO.class);
        opcUaPointMapper.insert(point);
        return point.getId();
    }

    @Override
    public void updateDeviceOpcUaPoint(IotDeviceOpcUaPointSaveReqVO updateReqVO) {
        // 1.1 校验存在，且属于请求的设备（禁止把点位改到其它设备下）
        IotDeviceOpcUaPointDO point = validateDeviceOpcUaPointExists(updateReqVO.getId());
        if (ObjUtil.notEqual(point.getDeviceId(), updateReqVO.getDeviceId())) {
            throw exception(DEVICE_OPC_UA_POINT_DEVICE_MISMATCH);
        }
        // 1.2 校验设备、产品协议、物模型属性、NodeId、唯一性
        validateDeviceOpcUaPoint(updateReqVO, updateReqVO.getId());

        // 2. 更新
        opcUaPointMapper.updateById(BeanUtils.toBean(updateReqVO, IotDeviceOpcUaPointDO.class));
    }

    @Override
    public void deleteDeviceOpcUaPoint(Long id) {
        // 校验存在
        validateDeviceOpcUaPointExists(id);
        // 删除（逻辑删除）
        opcUaPointMapper.deleteById(id);
    }

    private IotDeviceOpcUaPointDO validateDeviceOpcUaPointExists(Long id) {
        IotDeviceOpcUaPointDO point = opcUaPointMapper.selectById(id);
        if (point == null) {
            throw exception(DEVICE_OPC_UA_POINT_NOT_EXISTS);
        }
        return point;
    }

    private void validateDeviceOpcUaPoint(IotDeviceOpcUaPointSaveReqVO reqVO, Long excludeId) {
        // 1.1 校验设备存在
        IotDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        // 1.2 校验产品协议是 OPC UA Client
        IotProductDO product = productService.getProduct(device.getProductId());
        if (product == null || ObjUtil.notEqual(IotProtocolTypeEnum.OPC_UA_CLIENT.getType(), product.getProtocolType())) {
            throw exception(DEVICE_OPC_UA_CONFIG_PROTOCOL_INVALID);
        }
        // 2. 校验物模型：存在、属于设备所属产品、类型为属性
        IotThingModelDO thingModel = thingModelService.getThingModel(reqVO.getThingModelId());
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(thingModel.getProductId(), device.getProductId())
                || ObjUtil.notEqual(thingModel.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            throw exception(DEVICE_OPC_UA_POINT_THING_MODEL_INVALID);
        }
        // 2.2 校验可写：默认不可写；可写时物模型属性需要为读写
        reqVO.setWritable(Boolean.TRUE.equals(reqVO.getWritable()));
        if (reqVO.getWritable() && (thingModel.getProperty() == null || ObjUtil.notEqual(
                IotThingModelAccessModeEnum.READ_WRITE.getMode(), thingModel.getProperty().getAccessMode()))) {
            throw exception(DEVICE_OPC_UA_POINT_WRITABLE_INVALID);
        }
        // 3. 校验 NodeId 语法，不允许首尾空白（命名空间、标识由网关在会话激活后解析）
        if (!IotOpcUaUtils.isValidNodeId(reqVO.getNodeId())) {
            throw exception(DEVICE_OPC_UA_POINT_NODE_ID_INVALID, reqVO.getNodeId());
        }
        // 4. 校验同一设备下物模型属性唯一（只看未删除的记录：逻辑删除后可以重建同一点位；与 Modbus 一致不加锁，
        //    并发保存产生的重复点位不会导致报错：查询取编号最小的一条，网关配置接口按物模型属性去重）
        IotDeviceOpcUaPointDO point = opcUaPointMapper.selectByDeviceIdAndThingModelId(
                reqVO.getDeviceId(), reqVO.getThingModelId());
        if (point != null && ObjUtil.notEqual(point.getId(), excludeId)) {
            throw exception(DEVICE_OPC_UA_POINT_EXISTS);
        }
    }

    @Override
    public IotDeviceOpcUaPointDO getDeviceOpcUaPoint(Long id) {
        return opcUaPointMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceOpcUaPointDO> getDeviceOpcUaPointPage(IotDeviceOpcUaPointPageReqVO pageReqVO) {
        return opcUaPointMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<Long, List<IotDeviceOpcUaPointDO>> getEnabledDeviceOpcUaPointMapByDeviceIds(Collection<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        List<IotDeviceOpcUaPointDO> pointList = opcUaPointMapper.selectListByDeviceIdsAndStatus(
                deviceIds, CommonStatusEnum.ENABLE.getStatus());
        return convertMultiMap(pointList, IotDeviceOpcUaPointDO::getDeviceId);
    }

    @Override
    public Map<Long, IotThingModelDO> getPropertyThingModelMap(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Collections.emptyMap();
        }
        Map<Long, IotThingModelDO> result = new HashMap<>();
        for (Long productId : new HashSet<>(productIds)) {
            thingModelService.getThingModelListByProductIdAndType(productId, IotThingModelTypeEnum.PROPERTY.getType())
                    .forEach(thingModel -> result.put(thingModel.getId(), thingModel));
        }
        return result;
    }

}
