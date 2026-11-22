package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaPointSaveReqVO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.ThingModelProperty;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceOpcUaPointMapper;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelTypeEnum;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceOpcUaPointServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(IotDeviceOpcUaPointServiceImpl.class)
public class IotDeviceOpcUaPointServiceImplTest extends BaseDbUnitTest {

    private static final Integer COLLECT_POLL = IotOpcUaCollectModeEnum.POLL.getMode();
    private static final Integer COLLECT_SUBSCRIBE = IotOpcUaCollectModeEnum.SUBSCRIBE.getMode();

    private static final Long DEVICE_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long THING_MODEL_ID = 100L;

    @Resource
    private IotDeviceOpcUaPointServiceImpl opcUaPointService;

    @Resource
    private IotDeviceOpcUaPointMapper opcUaPointMapper;

    @MockitoBean
    private IotDeviceService deviceService;
    @MockitoBean
    private IotProductService productService;
    @MockitoBean
    private IotThingModelService thingModelService;

    @BeforeEach
    public void setUp() {
        when(deviceService.validateDeviceExists(eq(DEVICE_ID)))
                .thenReturn(IotDeviceDO.builder().id(DEVICE_ID).productId(PRODUCT_ID).build());
        when(productService.getProduct(eq(PRODUCT_ID))).thenReturn(IotProductDO.builder().id(PRODUCT_ID)
                .protocolType(IotProtocolTypeEnum.OPC_UA_CLIENT.getType()).build());
        mockThingModel(THING_MODEL_ID, PRODUCT_ID, IotThingModelTypeEnum.PROPERTY.getType());
    }

    @Test
    public void testCreateDeviceOpcUaPoint_success() {
        // 准备参数
        IotDeviceOpcUaPointSaveReqVO reqVO = newSaveReqVO().setNodeId("nsu=urn:plc;s=Line1.Temperature");

        // 调用
        Long id = opcUaPointService.createDeviceOpcUaPoint(reqVO);

        // 断言
        IotDeviceOpcUaPointDO point = opcUaPointMapper.selectById(id);
        assertPojoEquals(reqVO, point, "id");
        assertEquals("nsu=urn:plc;s=Line1.Temperature", point.getNodeId());
    }

    @Test
    public void testCreateDeviceOpcUaPoint_thingModelInvalid() {
        // 物模型不存在
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(999L)),
                THING_MODEL_NOT_EXISTS);
        // 其它产品的物模型
        mockThingModel(101L, 20L, IotThingModelTypeEnum.PROPERTY.getType());
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(101L)),
                DEVICE_OPC_UA_POINT_THING_MODEL_INVALID);
        // 不是属性（服务）
        mockThingModel(102L, PRODUCT_ID, IotThingModelTypeEnum.SERVICE.getType());
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(102L)),
                DEVICE_OPC_UA_POINT_THING_MODEL_INVALID);
    }

    @Test
    public void testCreateDeviceOpcUaPoint_writable() {
        // 默认不可写：未传 writable 时保存为 false
        Long id = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setWritable(null));
        assertFalse(opcUaPointMapper.selectById(id).getWritable());

        // 物模型属性为读写：可以设为可写
        mockThingModel(107L, PRODUCT_ID, IotThingModelTypeEnum.PROPERTY.getType(), IotThingModelAccessModeEnum.READ_WRITE);
        Long writableId = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(107L).setWritable(true));
        assertTrue(opcUaPointMapper.selectById(writableId).getWritable());

        // 物模型属性为只读、没有属性定义：不能设为可写（新增、修改都校验）
        mockThingModel(108L, PRODUCT_ID, IotThingModelTypeEnum.PROPERTY.getType(), IotThingModelAccessModeEnum.READ_ONLY);
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(108L)
                .setWritable(true)), DEVICE_OPC_UA_POINT_WRITABLE_INVALID);
        assertServiceException(() -> opcUaPointService.updateDeviceOpcUaPoint(newSaveReqVO().setId(id)
                .setWritable(true)), DEVICE_OPC_UA_POINT_WRITABLE_INVALID);
        assertFalse(opcUaPointMapper.selectById(id).getWritable());
        // 只读属性不设可写，可以正常保存
        opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(108L).setWritable(false));
    }

    @Test
    public void testCreateDeviceOpcUaPoint_nodeIdInvalid() {
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setNodeId("ns=70000;i=1")),
                DEVICE_OPC_UA_POINT_NODE_ID_INVALID, "ns=70000;i=1");
        // 首尾空白：拒绝，不自动去掉
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setNodeId(" ns=2;i=1 ")),
                DEVICE_OPC_UA_POINT_NODE_ID_INVALID, " ns=2;i=1 ");
    }

    @Test
    public void testCreateDeviceOpcUaPoint_protocolInvalid() {
        // mock 方法：设备所属产品不是 OPC UA Client
        when(productService.getProduct(eq(PRODUCT_ID))).thenReturn(IotProductDO.builder().id(PRODUCT_ID)
                .protocolType(IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType()).build());

        // 调用，并断言异常：新增、修改都校验
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO()),
                DEVICE_OPC_UA_CONFIG_PROTOCOL_INVALID);
        // 产品不存在
        when(productService.getProduct(eq(PRODUCT_ID))).thenReturn(null);
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO()),
                DEVICE_OPC_UA_CONFIG_PROTOCOL_INVALID);
        assertTrue(opcUaPointMapper.selectList().isEmpty());
    }

    @Test
    public void testCreateDeviceOpcUaPoint_toleranceDuplicate() {
        // mock 数据：同一设备同一物模型属性已有两个未删除的点位（应用层唯一不加锁，并发保存可能产生）
        IotDeviceOpcUaPointDO first = BeanUtils.toBean(newSaveReqVO(), IotDeviceOpcUaPointDO.class);
        opcUaPointMapper.insert(first);
        opcUaPointMapper.insert(BeanUtils.toBean(newSaveReqVO(), IotDeviceOpcUaPointDO.class));

        // 调用：查询不抛 TooManyResultsException，按已存在处理；修改第一条（排除自己）不受影响
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO()),
                DEVICE_OPC_UA_POINT_EXISTS);
        opcUaPointService.updateDeviceOpcUaPoint(newSaveReqVO().setId(first.getId()).setNodeId("ns=2;i=7"));
        assertEquals("ns=2;i=7", opcUaPointMapper.selectById(first.getId()).getNodeId());
    }

    @Test
    public void testCreateDeviceOpcUaPoint_duplicateAndLogicalDelete() {
        // 同一设备同一物模型属性只能有一个未删除的点位
        Long id = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO());
        assertServiceException(() -> opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO()),
                DEVICE_OPC_UA_POINT_EXISTS);

        // 逻辑删除后可以重建
        opcUaPointService.deleteDeviceOpcUaPoint(id);
        assertNull(opcUaPointService.getDeviceOpcUaPoint(id));
        Long newId = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO());
        assertNotEquals(id, newId);
    }

    @Test
    public void testUpdateDeviceOpcUaPoint() {
        // 准备数据
        Long id = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO());
        mockThingModel(103L, PRODUCT_ID, IotThingModelTypeEnum.PROPERTY.getType());
        Long otherId = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(103L));

        // 调用：修改自身（排除自己，不算重复）
        opcUaPointService.updateDeviceOpcUaPoint(newSaveReqVO().setId(id).setNodeId("ns=2;i=1001")
                .setCollectMode(COLLECT_POLL).setCollectInterval(500));
        IotDeviceOpcUaPointDO point = opcUaPointMapper.selectById(id);
        assertEquals("ns=2;i=1001", point.getNodeId());
        assertEquals(COLLECT_POLL, point.getCollectMode());
        assertEquals(500, point.getCollectInterval());

        // 调用：改成其它点位已使用的物模型属性
        assertServiceException(() -> opcUaPointService.updateDeviceOpcUaPoint(newSaveReqVO().setId(otherId)),
                DEVICE_OPC_UA_POINT_EXISTS);
        // 调用：不存在
        assertServiceException(() -> opcUaPointService.updateDeviceOpcUaPoint(newSaveReqVO().setId(-1L)),
                DEVICE_OPC_UA_POINT_NOT_EXISTS);
    }

    @Test
    public void testUpdateDeviceOpcUaPoint_deviceMismatch() {
        // 准备数据：设备 1 的点位；设备 2 是同一产品下的合法 OPC UA 设备
        Long id = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO());
        when(deviceService.validateDeviceExists(eq(2L)))
                .thenReturn(IotDeviceDO.builder().id(2L).productId(PRODUCT_ID).build());

        // 调用，并断言异常：不能通过修改把点位改写到其它设备下
        assertServiceException(() -> opcUaPointService.updateDeviceOpcUaPoint(newSaveReqVO().setId(id)
                .setDeviceId(2L).setNodeId("ns=2;i=1001")), DEVICE_OPC_UA_POINT_DEVICE_MISMATCH);
        // 断言：原记录未被改写，且未进入设备锁定和后续校验
        IotDeviceOpcUaPointDO point = opcUaPointMapper.selectById(id);
        assertEquals(DEVICE_ID, point.getDeviceId());
        assertEquals("nsu=urn:plc;s=Line1.Temperature", point.getNodeId());
        verify(deviceService, never()).validateDeviceExists(eq(2L));
    }

    @Test
    public void testDeleteDeviceOpcUaPoint_notExists() {
        assertServiceException(() -> opcUaPointService.deleteDeviceOpcUaPoint(-1L), DEVICE_OPC_UA_POINT_NOT_EXISTS);
    }

    @Test
    public void testGetDeviceOpcUaPointPage() {
        // 准备数据
        opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setNodeId("nsu=urn:plc;s=Temperature"));
        mockThingModel(104L, PRODUCT_ID, IotThingModelTypeEnum.PROPERTY.getType());
        opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(104L).setNodeId("i=2258"));

        // 调用
        IotDeviceOpcUaPointPageReqVO reqVO = new IotDeviceOpcUaPointPageReqVO();
        reqVO.setDeviceId(DEVICE_ID);
        reqVO.setNodeId("Temperature");
        PageResult<IotDeviceOpcUaPointDO> page = opcUaPointService.getDeviceOpcUaPointPage(reqVO);

        // 断言
        assertEquals(1, page.getTotal());
        assertEquals("nsu=urn:plc;s=Temperature", page.getList().get(0).getNodeId());
    }

    @Test
    public void testGetEnabledDeviceOpcUaPointMapByDeviceIds() {
        // 准备数据：启用、禁用、已删除
        Long enabledId = opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO());
        mockThingModel(105L, PRODUCT_ID, IotThingModelTypeEnum.PROPERTY.getType());
        opcUaPointService.createDeviceOpcUaPoint(newSaveReqVO().setThingModelId(105L)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        IotDeviceOpcUaPointDO deleted = IotDeviceOpcUaPointDO.builder().deviceId(2L).thingModelId(106L)
                .nodeId("i=1").collectMode(COLLECT_POLL).collectInterval(1000)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        opcUaPointMapper.insert(deleted);
        opcUaPointMapper.deleteById(deleted.getId());

        // 调用
        Map<Long, List<IotDeviceOpcUaPointDO>> result = opcUaPointService.getEnabledDeviceOpcUaPointMapByDeviceIds(
                Arrays.asList(DEVICE_ID, 2L));

        // 断言
        assertEquals(1, result.size());
        assertEquals(1, result.get(DEVICE_ID).size());
        assertEquals(enabledId, result.get(DEVICE_ID).get(0).getId());
    }

    // ========== 工具方法 ==========

    private void mockThingModel(Long id, Long productId, Integer type) {
        when(thingModelService.getThingModel(eq(id))).thenReturn(IotThingModelDO.builder().id(id)
                .productId(productId).type(type).identifier("p" + id).name("属性" + id).build());
    }

    private void mockThingModel(Long id, Long productId, Integer type, IotThingModelAccessModeEnum accessMode) {
        ThingModelProperty property = new ThingModelProperty();
        property.setIdentifier("p" + id);
        property.setAccessMode(accessMode.getMode());
        when(thingModelService.getThingModel(eq(id))).thenReturn(IotThingModelDO.builder().id(id)
                .productId(productId).type(type).identifier("p" + id).name("属性" + id).property(property).build());
    }

    private static IotDeviceOpcUaPointSaveReqVO newSaveReqVO() {
        return new IotDeviceOpcUaPointSaveReqVO().setDeviceId(DEVICE_ID).setThingModelId(THING_MODEL_ID)
                .setNodeId("nsu=urn:plc;s=Line1.Temperature").setCollectMode(COLLECT_SUBSCRIBE).setCollectInterval(1000)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
