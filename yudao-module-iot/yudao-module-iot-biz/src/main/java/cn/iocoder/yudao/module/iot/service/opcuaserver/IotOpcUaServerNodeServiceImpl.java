package cn.iocoder.yudao.module.iot.service.opcuaserver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver.IotOpcUaServerNodeMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT OPC UA Server 节点 Service 实现类
 * <p>
 * 租户隔离：配置、设备都按当前租户查询（多租户插件自动拼接 tenant_id），因此节点只能引用本租户的配置和设备
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotOpcUaServerNodeServiceImpl implements IotOpcUaServerNodeService {

    @Resource
    private IotOpcUaServerNodeMapper opcUaServerNodeMapper;

    @Resource
    private IotOpcUaServerConfigService opcUaServerConfigService;
    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Long createOpcUaServerNode(IotOpcUaServerNodeSaveReqVO createReqVO) {
        // 1. 校验配置、设备、物模型属性、唯一性
        validateOpcUaServerNode(createReqVO, null);

        // 2. 插入
        IotOpcUaServerNodeDO node = BeanUtils.toBean(createReqVO, IotOpcUaServerNodeDO.class, o -> o.setId(null));
        opcUaServerNodeMapper.insert(node);
        return node.getId();
    }

    @Override
    public void updateOpcUaServerNode(IotOpcUaServerNodeSaveReqVO updateReqVO) {
        // 1.1 校验存在，且属于请求的配置（禁止把节点改到其它配置下）
        IotOpcUaServerNodeDO node = validateOpcUaServerNodeExists(updateReqVO.getId());
        if (ObjUtil.notEqual(node.getConfigId(), updateReqVO.getConfigId())) {
            throw exception(OPC_UA_SERVER_NODE_CONFIG_MISMATCH);
        }
        // 1.2 校验配置、设备、物模型属性、唯一性
        validateOpcUaServerNode(updateReqVO, updateReqVO.getId());

        // 2. 更新
        opcUaServerNodeMapper.updateById(BeanUtils.toBean(updateReqVO, IotOpcUaServerNodeDO.class));
    }

    @Override
    public void deleteOpcUaServerNode(Long id) {
        // 校验存在
        validateOpcUaServerNodeExists(id);
        // 删除（逻辑删除）
        opcUaServerNodeMapper.deleteById(id);
    }

    private IotOpcUaServerNodeDO validateOpcUaServerNodeExists(Long id) {
        IotOpcUaServerNodeDO node = opcUaServerNodeMapper.selectById(id);
        if (node == null) {
            throw exception(OPC_UA_SERVER_NODE_NOT_EXISTS);
        }
        return node;
    }

    private void validateOpcUaServerNode(IotOpcUaServerNodeSaveReqVO reqVO, Long excludeId) {
        // 1. 校验配置、设备存在（都在当前租户下）
        opcUaServerConfigService.validateOpcUaServerConfigExists(reqVO.getConfigId());
        IotDeviceDO device = deviceService.validateDeviceExists(reqVO.getDeviceId());
        // 2. 校验物模型：存在、属于设备所属产品、类型为属性
        IotThingModelDO thingModel = thingModelService.getThingModel(reqVO.getThingModelId());
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(thingModel.getProductId(), device.getProductId())
                || ObjUtil.notEqual(thingModel.getType(), IotThingModelTypeEnum.PROPERTY.getType())) {
            throw exception(OPC_UA_SERVER_NODE_THING_MODEL_INVALID);
        }
        // 3.1 校验同一配置下浏览名称唯一（NodeId 由浏览名称生成；格式由 IotOpcUaServerNodeSaveReqVO 校验，不含空白）
        IotOpcUaServerNodeDO node = opcUaServerNodeMapper.selectByConfigIdAndBrowseName(
                reqVO.getConfigId(), reqVO.getBrowseName());
        if (node != null && ObjUtil.notEqual(node.getId(), excludeId)) {
            throw exception(OPC_UA_SERVER_NODE_BROWSE_NAME_EXISTS);
        }
        // 3.2 校验同一配置下设备属性唯一
        node = opcUaServerNodeMapper.selectByConfigIdAndDeviceIdAndThingModelId(
                reqVO.getConfigId(), reqVO.getDeviceId(), reqVO.getThingModelId());
        if (node != null && ObjUtil.notEqual(node.getId(), excludeId)) {
            throw exception(OPC_UA_SERVER_NODE_EXISTS);
        }
    }

    @Override
    public IotOpcUaServerNodeDO getOpcUaServerNode(Long id) {
        return opcUaServerNodeMapper.selectById(id);
    }

    @Override
    public PageResult<IotOpcUaServerNodeDO> getOpcUaServerNodePage(IotOpcUaServerNodePageReqVO pageReqVO) {
        return opcUaServerNodeMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<Long, List<IotOpcUaServerNodeDO>> getEnabledOpcUaServerNodeMapByConfigIds(Collection<Long> configIds) {
        if (CollUtil.isEmpty(configIds)) {
            return Collections.emptyMap();
        }
        List<IotOpcUaServerNodeDO> nodeList = opcUaServerNodeMapper.selectListByConfigIdsAndStatus(
                configIds, CommonStatusEnum.ENABLE.getStatus());
        return convertMultiMap(nodeList, IotOpcUaServerNodeDO::getConfigId);
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
