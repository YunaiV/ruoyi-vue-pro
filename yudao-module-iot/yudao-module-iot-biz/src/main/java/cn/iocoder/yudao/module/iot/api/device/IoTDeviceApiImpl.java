package cn.iocoder.yudao.module.iot.api.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.RpcConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.*;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterReqDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.core.topic.auth.IotSubDeviceRegisterRespDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceOpcUaConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceOpcUaPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.property.IotDevicePropertyService;
import cn.iocoder.yudao.module.iot.service.opcuaserver.IotOpcUaServerConfigService;
import cn.iocoder.yudao.module.iot.service.opcuaserver.IotOpcUaServerNodeService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * IoT 设备 API 实现类
 *
 * @author haohao
 */
@RestController
@Validated
@Slf4j
@Primary // 保证优先匹配，因为 yudao-iot-gateway 也有 IotDeviceCommonApi 的实现，并且也可能会被 biz 引入
public class IoTDeviceApiImpl implements IotDeviceCommonApi {

    @Resource
    private IotDeviceService deviceService;
    @Resource
    private IotProductService productService;
    @Resource
    private IotDevicePropertyService devicePropertyService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceModbusConfigService modbusConfigService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceModbusPointService modbusPointService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceOpcUaConfigService opcUaConfigService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotDeviceOpcUaPointService opcUaPointService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotOpcUaServerConfigService opcUaServerConfigService;
    @Resource
    @Lazy // 延迟加载，解决循环依赖
    private IotOpcUaServerNodeService opcUaServerNodeService;

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/auth")
    @PermitAll
    public CommonResult<Boolean> authDevice(@RequestBody IotDeviceAuthReqDTO authReqDTO) {
        return success(deviceService.authDevice(authReqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/get") // 特殊：方便调用，暂时使用 POST，实际更推荐 GET
    @PermitAll
    public CommonResult<IotDeviceRespDTO> getDevice(@RequestBody IotDeviceGetReqDTO getReqDTO) {
        IotDeviceDO device = getReqDTO.getId() != null ? deviceService.getDeviceFromCache(getReqDTO.getId())
                : deviceService.getDeviceFromCache(getReqDTO.getProductKey(), getReqDTO.getDeviceName());
        return success(BeanUtils.toBean(device, IotDeviceRespDTO.class, deviceDTO -> {
            IotProductDO product = productService.getProductFromCache(deviceDTO.getProductId());
            if (product != null) {
                // 数据解析脚本随设备信息返回（网关按设备缓存），只在启用时返回内容
                deviceDTO.setProtocolType(product.getProtocolType()).setSerializeType(product.getSerializeType())
                        .setScriptEnabled(product.getScriptEnabled())
                        .setScriptContent(Boolean.TRUE.equals(product.getScriptEnabled()) ? product.getScriptContent() : null);
            }
        }));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/modbus/config-list")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotModbusDeviceConfigRespDTO>> getModbusDeviceConfigList(
            @RequestBody IotModbusDeviceConfigListReqDTO listReqDTO) {
        // 1. 获取 Modbus 连接配置
        List<IotDeviceModbusConfigDO> configList = modbusConfigService.getDeviceModbusConfigList(listReqDTO);
        if (CollUtil.isEmpty(configList)) {
            return success(new ArrayList<>());
        }

        // 2. 组装返回结果
        Set<Long> deviceIds = convertSet(configList, IotDeviceModbusConfigDO::getDeviceId);
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(deviceIds);
        if (CollUtil.isEmpty(deviceMap)) {
            return success(new ArrayList<>());
        }
        Map<Long, List<IotDeviceModbusPointDO>> pointMap = modbusPointService.getEnabledDeviceModbusPointMapByDeviceIds(deviceIds);
        Map<Long, IotProductDO> productMap = productService.getProductMap(convertSet(deviceMap.values(), IotDeviceDO::getProductId));
        List<IotModbusDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        for (IotDeviceModbusConfigDO config : configList) {
            // 3.1 获取设备信息
            IotDeviceDO device = deviceMap.get(config.getDeviceId());
            if (device == null) {
                continue;
            }
            // 3.2 按 protocolType 筛选（如果非空）
            if (StrUtil.isNotEmpty(listReqDTO.getProtocolType())) {
                IotProductDO product = productMap.get(device.getProductId());
                if (product == null || ObjUtil.notEqual(listReqDTO.getProtocolType(), product.getProtocolType())) {
                    continue;
                }
            }
            // 3.3 获取启用的点位列表
            //     点位为空也返回：连接配置有效即可认证、收发透传（与脚本开关无关）；没有点位时，轮询调度器本身会跳过
            List<IotDeviceModbusPointDO> pointList = pointMap.getOrDefault(config.getDeviceId(), Collections.emptyList());

            // 3.4 构建 IotModbusDeviceConfigRespDTO 对象
            IotModbusDeviceConfigRespDTO configDTO = BeanUtils.toBean(config, IotModbusDeviceConfigRespDTO.class, o ->
                    o.setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                            .setPoints(BeanUtils.toBean(pointList, IotModbusPointRespDTO.class)));
            result.add(configDTO);
        }
        return success(result);
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/opc-ua/config-list")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotOpcUaDeviceConfigRespDTO>> getOpcUaDeviceConfigList(
            @RequestBody IotOpcUaDeviceConfigListReqDTO listReqDTO) {
        // 1. 获取 OPC UA 连接配置（单网关部署：按启用状态和协议类型返回全部配置，不做网关归属过滤）
        List<IotDeviceOpcUaConfigDO> configList = opcUaConfigService.getDeviceOpcUaConfigList(listReqDTO);
        if (CollUtil.isEmpty(configList)) {
            return success(new ArrayList<>());
        }

        // 2. 获取设备、产品、启用的点位，以及点位关联的物模型属性
        Set<Long> deviceIds = convertSet(configList, IotDeviceOpcUaConfigDO::getDeviceId);
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(deviceIds);
        if (CollUtil.isEmpty(deviceMap)) {
            return success(new ArrayList<>());
        }
        Set<Long> productIds = convertSet(deviceMap.values(), IotDeviceDO::getProductId);
        Map<Long, IotProductDO> productMap = productService.getProductMap(productIds);
        Map<Long, List<IotDeviceOpcUaPointDO>> pointMap = opcUaPointService.getEnabledDeviceOpcUaPointMapByDeviceIds(deviceIds);
        Map<Long, IotThingModelDO> thingModelMap = opcUaPointService.getPropertyThingModelMap(productIds);

        // 3. 组装返回结果：不含口令，只有凭据别名
        List<IotOpcUaDeviceConfigRespDTO> result = new ArrayList<>(configList.size());
        Set<Long> returnedDeviceIds = new HashSet<>();
        for (IotDeviceOpcUaConfigDO config : configList) {
            // 3.1 获取设备信息，按 protocolType 筛选（如果非空）；同一设备万一有多条配置，只返回编号最小的一条
            IotDeviceDO device = deviceMap.get(config.getDeviceId());
            if (device == null || !returnedDeviceIds.add(config.getDeviceId())) {
                continue;
            }
            if (StrUtil.isNotEmpty(listReqDTO.getProtocolType())) {
                IotProductDO product = productMap.get(device.getProductId());
                if (product == null || ObjUtil.notEqual(listReqDTO.getProtocolType(), product.getProtocolType())) {
                    continue;
                }
            }
            // 3.2 点位关联物模型属性：物模型已删除、不再是属性、或不属于设备所属产品时，跳过该点位；
            //     同一物模型属性万一有多个点位，只返回编号最小的一个
            List<IotOpcUaPointRespDTO> points = new ArrayList<>();
            Set<Long> thingModelIds = new HashSet<>();
            for (IotDeviceOpcUaPointDO point : pointMap.getOrDefault(config.getDeviceId(), Collections.emptyList())) {
                IotThingModelDO thingModel = thingModelMap.get(point.getThingModelId());
                if (thingModel == null || ObjUtil.notEqual(thingModel.getProductId(), device.getProductId())
                        || !thingModelIds.add(point.getThingModelId())) {
                    continue;
                }
                // 可写：点位开启可写，且物模型属性当前仍是读写（物模型改为只读后，不下发点位上残留的 writable=true）
                boolean writable = Boolean.TRUE.equals(point.getWritable()) && thingModel.getProperty() != null
                        && ObjUtil.equal(IotThingModelAccessModeEnum.READ_WRITE.getMode(), thingModel.getProperty().getAccessMode());
                points.add(BeanUtils.toBean(point, IotOpcUaPointRespDTO.class, o -> {
                    o.setIdentifier(thingModel.getIdentifier());
                    o.setWritable(writable);
                }));
            }
            // 3.3 点位为空也返回：会话与上下线照常维护，没有点位时不采集
            result.add(BeanUtils.toBean(config, IotOpcUaDeviceConfigRespDTO.class, o ->
                    o.setTenantId(device.getTenantId()).setProductKey(device.getProductKey())
                            .setDeviceName(device.getDeviceName()).setPoints(points)));
        }
        return success(result);
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/opc-ua-server/config-list")
    @PermitAll
    @TenantIgnore
    public CommonResult<List<IotOpcUaServerConfigRespDTO>> getOpcUaServerConfigList() {
        // 1. 获取全部租户启用的配置（网关按配置编号隔离会话可见的节点，按凭据的 tenant-ids 校验配置的租户）
        List<IotOpcUaServerConfigDO> configList = opcUaServerConfigService.getEnabledOpcUaServerConfigList();
        if (CollUtil.isEmpty(configList)) {
            return success(new ArrayList<>());
        }

        // 2. 获取启用的节点，以及节点关联的设备、物模型属性
        Map<Long, List<IotOpcUaServerNodeDO>> nodeMap = opcUaServerNodeService.getEnabledOpcUaServerNodeMapByConfigIds(
                convertSet(configList, IotOpcUaServerConfigDO::getId));
        Set<Long> deviceIds = new HashSet<>();
        nodeMap.values().forEach(nodes -> deviceIds.addAll(convertSet(nodes, IotOpcUaServerNodeDO::getDeviceId)));
        Map<Long, IotDeviceDO> deviceMap = CollUtil.isEmpty(deviceIds) ? Collections.emptyMap()
                : deviceService.getDeviceMap(deviceIds);
        Map<Long, IotThingModelDO> thingModelMap = opcUaServerNodeService.getPropertyThingModelMap(
                convertSet(deviceMap.values(), IotDeviceDO::getProductId));

        // 3. 组装返回结果：不含口令，只有凭据别名；同一凭据别名万一有多条配置，只返回编号最小的一条
        List<IotOpcUaServerConfigRespDTO> result = new ArrayList<>(configList.size());
        Set<String> returnedAliases = new HashSet<>();
        for (IotOpcUaServerConfigDO config : configList) {
            if (!returnedAliases.add(config.getCredentialAlias())) {
                continue;
            }
            // 节点：设备、物模型属性必须与配置同租户，且物模型仍是设备所属产品的属性；浏览名称重复时只保留编号最小的一个
            List<IotOpcUaServerNodeRespDTO> nodes = new ArrayList<>();
            Set<String> browseNames = new HashSet<>();
            for (IotOpcUaServerNodeDO node : nodeMap.getOrDefault(config.getId(), Collections.emptyList())) {
                IotDeviceDO device = deviceMap.get(node.getDeviceId());
                IotThingModelDO thingModel = thingModelMap.get(node.getThingModelId());
                if (device == null || ObjUtil.notEqual(device.getTenantId(), config.getTenantId())
                        || thingModel == null || ObjUtil.notEqual(thingModel.getProductId(), device.getProductId())
                        || !browseNames.add(node.getBrowseName())) {
                    continue;
                }
                nodes.add(BeanUtils.toBean(node, IotOpcUaServerNodeRespDTO.class, o -> o
                        .setProductKey(device.getProductKey()).setDeviceName(device.getDeviceName())
                        .setIdentifier(thingModel.getIdentifier())
                        .setDataType(thingModel.getProperty() != null ? thingModel.getProperty().getDataType() : null)));
            }
            result.add(BeanUtils.toBean(config, IotOpcUaServerConfigRespDTO.class, o -> o.setNodes(nodes)));
        }
        return success(result);
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/register")
    @PermitAll
    public CommonResult<IotDeviceRegisterRespDTO> registerDevice(@RequestBody IotDeviceRegisterReqDTO reqDTO) {
        return success(deviceService.registerDevice(reqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/device/register-sub")
    @PermitAll
    public CommonResult<List<IotSubDeviceRegisterRespDTO>> registerSubDevices(@RequestBody IotSubDeviceRegisterFullReqDTO reqDTO) {
        return success(deviceService.registerSubDevices(reqDTO));
    }

    @Override
    @PostMapping(RpcConstants.RPC_API_PREFIX + "/iot/gateway-server/keep-alive")
    @PermitAll // 无鉴权：前提是只有受信的网关能访问 RPC 接口
    @TenantIgnore // 网关级的全局数据，不按租户隔离；如果以后在这里访问 DB，需要重新评估租户约束
    public CommonResult<Boolean> keepAliveGatewayServers(@RequestBody IotGatewayServerAliveReqDTO reqDTO) {
        // 参数校验失败时，不会执行到这里，所以非法的批次不会写入任何一项
        int failCount = 0;
        for (Map.Entry<String, Long> entry : reqDTO.getStartTimes().entrySet()) {
            // 单项失败不影响其它项：失败明细只记录日志，整体结果返回 false
            try {
                devicePropertyService.updateGatewayServerStartTime(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                failCount++;
                log.error("[keepAliveGatewayServers][serverId({}) startTime({}) 续期存活标记失败]", entry.getKey(), entry.getValue(), e);
            }
        }
        return success(failCount == 0);
    }

}
