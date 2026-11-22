package cn.iocoder.yudao.module.iot.service.opcuaserver;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver.IotOpcUaServerNodeMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link IotOpcUaServerNodeServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(IotOpcUaServerNodeServiceImpl.class)
public class IotOpcUaServerNodeServiceImplTest extends BaseDbUnitTest {

    private static final Long CONFIG_ID = 1L;
    private static final Long DEVICE_ID = 10L;
    private static final Long PRODUCT_ID = 100L;

    @Resource
    private IotOpcUaServerNodeServiceImpl opcUaServerNodeService;

    @Resource
    private IotOpcUaServerNodeMapper opcUaServerNodeMapper;

    @MockitoBean
    private IotOpcUaServerConfigService opcUaServerConfigService;
    @MockitoBean
    private IotDeviceService deviceService;
    @MockitoBean
    private IotThingModelService thingModelService;

    @BeforeEach
    public void setUp() {
        when(deviceService.validateDeviceExists(eq(DEVICE_ID)))
                .thenReturn(IotDeviceDO.builder().id(DEVICE_ID).productId(PRODUCT_ID).build());
        when(thingModelService.getThingModel(eq(1001L))).thenReturn(IotThingModelDO.builder().id(1001L)
                .productId(PRODUCT_ID).type(IotThingModelTypeEnum.PROPERTY.getType()).identifier("temperature").build());
        when(thingModelService.getThingModel(eq(1002L))).thenReturn(IotThingModelDO.builder().id(1002L)
                .productId(PRODUCT_ID).type(IotThingModelTypeEnum.PROPERTY.getType()).identifier("humidity").build());
        when(thingModelService.getThingModel(eq(2001L))).thenReturn(IotThingModelDO.builder().id(2001L)
                .productId(PRODUCT_ID).type(IotThingModelTypeEnum.SERVICE.getType()).identifier("reboot").build());
        when(thingModelService.getThingModel(eq(3001L))).thenReturn(IotThingModelDO.builder().id(3001L)
                .productId(300L).type(IotThingModelTypeEnum.PROPERTY.getType()).identifier("other").build());
    }

    @Test
    public void testCreateOpcUaServerNode_success() {
        // 调用
        Long id = opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(1001L, "temperature"));
        // 断言
        IotOpcUaServerNodeDO node = opcUaServerNodeMapper.selectById(id);
        assertEquals("temperature", node.getBrowseName());
        assertEquals(CONFIG_ID, node.getConfigId());
        assertEquals(DEVICE_ID, node.getDeviceId());
    }

    @Test
    public void testCreateOpcUaServerNode_thingModelInvalid() {
        // 断言：非属性、其它产品的物模型都不能暴露
        assertServiceException(() -> opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(2001L, "reboot")),
                OPC_UA_SERVER_NODE_THING_MODEL_INVALID);
        assertServiceException(() -> opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(3001L, "other")),
                OPC_UA_SERVER_NODE_THING_MODEL_INVALID);
        assertServiceException(() -> opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(9999L, "missing")),
                THING_MODEL_NOT_EXISTS);
    }

    @Test
    public void testCreateOpcUaServerNode_duplicate() {
        opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(1001L, "temperature"));
        // 断言：同一配置下浏览名称唯一
        assertServiceException(() -> opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(1002L, "temperature")),
                OPC_UA_SERVER_NODE_BROWSE_NAME_EXISTS);
        // 断言：同一配置下设备属性唯一
        assertServiceException(() -> opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(1001L, "temperature-2")),
                OPC_UA_SERVER_NODE_EXISTS);
    }

    @Test
    public void testUpdateOpcUaServerNode_configMismatch() {
        Long id = opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(1001L, "temperature"));
        IotOpcUaServerNodeSaveReqVO reqVO = newSaveReqVO(1001L, "temperature");
        reqVO.setId(id);
        reqVO.setConfigId(2L);
        // 断言：不能把节点改到其它配置下
        assertServiceException(() -> opcUaServerNodeService.updateOpcUaServerNode(reqVO), OPC_UA_SERVER_NODE_CONFIG_MISMATCH);
    }

    @Test
    public void testGetEnabledOpcUaServerNodeMapByConfigIds() {
        opcUaServerNodeService.createOpcUaServerNode(newSaveReqVO(1001L, "temperature"));
        IotOpcUaServerNodeSaveReqVO disabled = newSaveReqVO(1002L, "humidity");
        disabled.setStatus(CommonStatusEnum.DISABLE.getStatus());
        opcUaServerNodeService.createOpcUaServerNode(disabled);

        // 断言：只返回启用的节点
        Map<Long, List<IotOpcUaServerNodeDO>> map = opcUaServerNodeService.getEnabledOpcUaServerNodeMapByConfigIds(Collections.singleton(CONFIG_ID));
        assertEquals(1, map.get(CONFIG_ID).size());
        assertEquals("temperature", map.get(CONFIG_ID).get(0).getBrowseName());
        assertTrue(opcUaServerNodeService.getEnabledOpcUaServerNodeMapByConfigIds(Collections.<Long>emptySet()).isEmpty());
    }

    private static IotOpcUaServerNodeSaveReqVO newSaveReqVO(Long thingModelId, String browseName) {
        IotOpcUaServerNodeSaveReqVO reqVO = new IotOpcUaServerNodeSaveReqVO();
        reqVO.setConfigId(CONFIG_ID);
        reqVO.setDeviceId(DEVICE_ID);
        reqVO.setThingModelId(thingModelId);
        reqVO.setBrowseName(browseName);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
