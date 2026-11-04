package cn.iocoder.yudao.module.iot.service.device;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointSaveReqVO;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusByteOrderEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFunctionCodeEnum;
import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusRawDataTypeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceModbusPointMapper;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// DONE @AI：对齐 system user test；也补齐之前缺的单测噢；（已按 AdminUserServiceImplTest 的随机对象、assertPojoEquals 风格重写，补齐创建、更新、删除、查询、分页、按设备查询的成功与异常用例）
/**
 * {@link IotDeviceModbusPointServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(IotDeviceModbusPointServiceImpl.class)
public class IotDeviceModbusPointServiceImplTest extends BaseDbUnitTest {

    @Resource
    private IotDeviceModbusPointServiceImpl modbusPointService;

    @Resource
    private IotDeviceModbusPointMapper modbusPointMapper;

    @MockitoBean
    private IotDeviceService deviceService;
    @MockitoBean
    private IotThingModelService thingModelService;

    @Test
    public void testCreateDeviceModbusPoint_success() {
        // 准备参数：位点位（FC03、BOOLEAN、1 个寄存器的第 8 位）
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> o.setBitIndex(8)).setId(null); // 避免 id 被赋值
        // mock 方法
        IotThingModelDO thingModel = mockThingModel(reqVO.getThingModelId(), randomString());

        // 调用
        Long id = modbusPointService.createDeviceModbusPoint(reqVO);
        // 断言：标识符、名称取自物模型
        IotDeviceModbusPointDO point = modbusPointMapper.selectById(id);
        assertPojoEquals(reqVO, point, "id");
        assertEquals(thingModel.getIdentifier(), point.getIdentifier());
        assertEquals(thingModel.getName(), point.getName());
    }

    @Test
    public void testCreateDeviceModbusPoint_withoutBitIndex() {
        // 准备参数：未配置位索引时，不校验位点位规则（FC01 线圈、INT16、2 个寄存器也可以保存）
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> {
            o.setFunctionCode(IotModbusFunctionCodeEnum.READ_COILS.getCode());
            o.setRawDataType(IotModbusRawDataTypeEnum.INT16.getType());
            o.setRegisterCount(2);
            o.setBitIndex(null);
        }).setId(null);
        // mock 方法
        mockThingModel(reqVO.getThingModelId(), randomString());

        // 调用
        Long id = modbusPointService.createDeviceModbusPoint(reqVO);
        // 断言
        assertPojoEquals(reqVO, modbusPointMapper.selectById(id), "id");
    }

    @Test
    public void testCreateDeviceModbusPoint_bitIndexInvalid() {
        // 准备参数：配置位索引，但不是 FC03/FC04、BOOLEAN、1 个寄存器
        List<IotDeviceModbusPointSaveReqVO> reqVOs = Arrays.asList(
                randomSaveReqVO(o -> o.setFunctionCode(IotModbusFunctionCodeEnum.READ_COILS.getCode())),
                randomSaveReqVO(o -> o.setFunctionCode(IotModbusFunctionCodeEnum.READ_DISCRETE_INPUTS.getCode())),
                randomSaveReqVO(o -> o.setRawDataType(IotModbusRawDataTypeEnum.INT16.getType())),
                randomSaveReqVO(o -> o.setRegisterCount(2)),
                randomSaveReqVO(o -> o.setRegisterCount(null)));
        for (IotDeviceModbusPointSaveReqVO reqVO : reqVOs) {
            reqVO.setId(null);
            // mock 方法
            mockThingModel(reqVO.getThingModelId(), randomString());

            // 调用，并断言异常
            assertServiceException(() -> modbusPointService.createDeviceModbusPoint(reqVO),
                    DEVICE_MODBUS_POINT_BIT_INDEX_INVALID);
        }
        // 断言：没有插入
        assertEquals(0, modbusPointMapper.selectCount());
    }

    @Test
    public void testCreateDeviceModbusPoint_deviceNotExists() {
        // 准备参数
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO().setId(null);
        // mock 方法
        when(deviceService.validateDeviceExists(eq(reqVO.getDeviceId()))).thenThrow(exception(DEVICE_NOT_EXISTS));

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.createDeviceModbusPoint(reqVO), DEVICE_NOT_EXISTS);
    }

    @Test
    public void testCreateDeviceModbusPoint_thingModelNotExists() {
        // 准备参数
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO().setId(null);

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.createDeviceModbusPoint(reqVO), THING_MODEL_NOT_EXISTS);
    }

    @Test
    public void testCreateDeviceModbusPoint_exists() {
        // mock 数据
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO();
        modbusPointMapper.insert(dbPoint);
        // 准备参数：同一设备、同一物模型标识符
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> o.setDeviceId(dbPoint.getDeviceId())).setId(null);
        // mock 方法
        mockThingModel(reqVO.getThingModelId(), dbPoint.getIdentifier());

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.createDeviceModbusPoint(reqVO), DEVICE_MODBUS_POINT_EXISTS);
    }

    @Test
    public void testUpdateDeviceModbusPoint_success() {
        // mock 数据：已配置第 8 位
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO(o -> o.setBitIndex(8));
        modbusPointMapper.insert(dbPoint);
        // 准备参数：改为第 3 位
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> {
            o.setId(dbPoint.getId());
            o.setBitIndex(3);
        });
        // mock 方法
        IotThingModelDO thingModel = mockThingModel(reqVO.getThingModelId(), randomString());

        // 调用
        modbusPointService.updateDeviceModbusPoint(reqVO);
        // 断言：位索引随实体写入（H2 与 MySQL 一样，SET 中列重复会直接报错）
        IotDeviceModbusPointDO point = modbusPointMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, point);
        assertEquals(thingModel.getIdentifier(), point.getIdentifier());
        assertEquals(thingModel.getName(), point.getName());
    }

    @Test
    public void testUpdateDeviceModbusPoint_clearBitIndex() {
        // mock 数据：已配置第 8 位
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO(o -> o.setBitIndex(8));
        modbusPointMapper.insert(dbPoint);
        // 准备参数：改为 INT16，位索引为空
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> {
            o.setId(dbPoint.getId());
            o.setRawDataType(IotModbusRawDataTypeEnum.INT16.getType());
            o.setBitIndex(null);
        });
        // mock 方法
        mockThingModel(reqVO.getThingModelId(), randomString());

        // 调用
        modbusPointService.updateDeviceModbusPoint(reqVO);
        // 断言：位索引被清空，其它字段照常更新
        IotDeviceModbusPointDO point = modbusPointMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, point);
        assertNull(point.getBitIndex());
    }

    @Test
    public void testUpdateDeviceModbusPoint_bitIndexInvalid() {
        // mock 数据
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO();
        modbusPointMapper.insert(dbPoint);
        // 准备参数：改为 FC01 线圈，仍带着位索引
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> {
            o.setId(dbPoint.getId());
            o.setFunctionCode(IotModbusFunctionCodeEnum.READ_COILS.getCode());
        });
        // mock 方法
        mockThingModel(reqVO.getThingModelId(), randomString());

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.updateDeviceModbusPoint(reqVO),
                DEVICE_MODBUS_POINT_BIT_INDEX_INVALID);
        // 断言：没有更新
        assertPojoEquals(dbPoint, modbusPointMapper.selectById(dbPoint.getId()));
    }

    @Test
    public void testUpdateDeviceModbusPoint_notExists() {
        // 准备参数
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO();

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.updateDeviceModbusPoint(reqVO), DEVICE_MODBUS_POINT_NOT_EXISTS);
    }

    @Test
    public void testUpdateDeviceModbusPoint_exists() {
        // mock 数据：同一设备下的两个点位
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO();
        modbusPointMapper.insert(dbPoint);
        IotDeviceModbusPointDO otherPoint = randomModbusPointDO(o -> o.setDeviceId(dbPoint.getDeviceId()));
        modbusPointMapper.insert(otherPoint);
        // 准备参数：改为另一个点位的物模型标识符
        IotDeviceModbusPointSaveReqVO reqVO = randomSaveReqVO(o -> {
            o.setId(dbPoint.getId());
            o.setDeviceId(dbPoint.getDeviceId());
        });
        // mock 方法
        mockThingModel(reqVO.getThingModelId(), otherPoint.getIdentifier());

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.updateDeviceModbusPoint(reqVO), DEVICE_MODBUS_POINT_EXISTS);
    }

    @Test
    public void testUpdateDeviceModbusPointByThingModel_keepBitIndex() {
        // mock 数据：已配置第 8 位
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO(o -> o.setBitIndex(8));
        modbusPointMapper.insert(dbPoint);
        // 准备参数
        String identifier = randomString();
        String name = randomString();

        // 调用：物模型改名
        modbusPointService.updateDeviceModbusPointByThingModel(dbPoint.getThingModelId(), identifier, name);
        // 断言：只更新标识符和名称，位索引等其它字段保留
        IotDeviceModbusPointDO point = modbusPointMapper.selectById(dbPoint.getId());
        assertPojoEquals(dbPoint, point, "identifier", "name", "updateTime", "updater");
        assertEquals(identifier, point.getIdentifier());
        assertEquals(name, point.getName());
        assertEquals(8, point.getBitIndex());
    }

    @Test
    public void testDeleteDeviceModbusPoint_success() {
        // mock 数据
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO();
        modbusPointMapper.insert(dbPoint);
        // 准备参数
        Long id = dbPoint.getId();

        // 调用
        modbusPointService.deleteDeviceModbusPoint(id);
        // 断言
        assertNull(modbusPointMapper.selectById(id));
    }

    @Test
    public void testDeleteDeviceModbusPoint_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> modbusPointService.deleteDeviceModbusPoint(id), DEVICE_MODBUS_POINT_NOT_EXISTS);
    }

    @Test
    public void testGetDeviceModbusPoint() {
        // mock 数据
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO();
        modbusPointMapper.insert(dbPoint);
        // 准备参数
        Long id = dbPoint.getId();

        // 调用
        IotDeviceModbusPointDO point = modbusPointService.getDeviceModbusPoint(id);
        // 断言
        assertPojoEquals(dbPoint, point);
    }

    @Test
    public void testGetDeviceModbusPointPage() {
        // mock 数据
        IotDeviceModbusPointDO dbPoint = randomModbusPointDO(o -> {
            o.setDeviceId(1L);
            o.setIdentifier("temperature");
            o.setName("温度");
            o.setFunctionCode(IotModbusFunctionCodeEnum.READ_HOLDING_REGISTERS.getCode());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        modbusPointMapper.insert(dbPoint);
        // 测试 deviceId 不匹配
        modbusPointMapper.insert(cloneIgnoreId(dbPoint, o -> o.setDeviceId(2L)));
        // 测试 identifier 不匹配
        modbusPointMapper.insert(cloneIgnoreId(dbPoint, o -> o.setIdentifier("humidity")));
        // 测试 name 不匹配
        modbusPointMapper.insert(cloneIgnoreId(dbPoint, o -> o.setName("湿度")));
        // 测试 functionCode 不匹配
        modbusPointMapper.insert(cloneIgnoreId(dbPoint, o -> o.setFunctionCode(IotModbusFunctionCodeEnum.READ_INPUT_REGISTERS.getCode())));
        // 测试 status 不匹配
        modbusPointMapper.insert(cloneIgnoreId(dbPoint, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        IotDeviceModbusPointPageReqVO reqVO = new IotDeviceModbusPointPageReqVO();
        reqVO.setDeviceId(1L);
        reqVO.setIdentifier("temp");
        reqVO.setName("温");
        reqVO.setFunctionCode(IotModbusFunctionCodeEnum.READ_HOLDING_REGISTERS.getCode());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<IotDeviceModbusPointDO> pageResult = modbusPointService.getDeviceModbusPointPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbPoint, pageResult.getList().get(0));
    }

    @Test
    public void testGetEnabledDeviceModbusPointMapByDeviceIds() {
        // mock 数据
        IotDeviceModbusPointDO point01 = randomModbusPointDO(o -> {
            o.setDeviceId(1L);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        modbusPointMapper.insert(point01);
        IotDeviceModbusPointDO point02 = randomModbusPointDO(o -> {
            o.setDeviceId(2L);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        modbusPointMapper.insert(point02);
        // 测试 status 不匹配
        modbusPointMapper.insert(cloneIgnoreId(point01, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 deviceId 不匹配
        modbusPointMapper.insert(cloneIgnoreId(point01, o -> o.setDeviceId(3L)));

        // 调用
        Map<Long, List<IotDeviceModbusPointDO>> pointMap = modbusPointService.getEnabledDeviceModbusPointMapByDeviceIds(
                Arrays.asList(1L, 2L));
        // 断言：按设备分组，只返回启用的点位
        assertEquals(2, pointMap.size());
        assertEquals(1, pointMap.get(1L).size());
        assertPojoEquals(point01, pointMap.get(1L).get(0));
        assertEquals(1, pointMap.get(2L).size());
        assertPojoEquals(point02, pointMap.get(2L).get(0));
    }

    @Test
    public void testGetEnabledDeviceModbusPointMapByDeviceIds_empty() {
        // 调用，并断言：设备编号为空时直接返回空
        assertTrue(modbusPointService.getEnabledDeviceModbusPointMapByDeviceIds(Collections.emptyList()).isEmpty());
        assertTrue(modbusPointService.getEnabledDeviceModbusPointMapByDeviceIds(singletonList(randomLongId())).isEmpty());
    }

    private IotThingModelDO mockThingModel(Long id, String identifier) {
        IotThingModelDO thingModel = new IotThingModelDO().setId(id).setIdentifier(identifier).setName(randomString());
        when(thingModelService.getThingModel(eq(id))).thenReturn(thingModel);
        return thingModel;
    }

    // ========== 随机对象 ==========

    /**
     * 随机点位：默认是位点位（FC03、BOOLEAN、1 个寄存器，位索引 0-15）
     */
    @SafeVarargs
    private static IotDeviceModbusPointDO randomModbusPointDO(Consumer<IotDeviceModbusPointDO>... consumers) {
        Consumer<IotDeviceModbusPointDO> consumer = (o) -> {
            o.setFunctionCode(IotModbusFunctionCodeEnum.READ_HOLDING_REGISTERS.getCode());
            o.setRegisterCount(1);
            o.setBitIndex(RandomUtil.randomInt(0, 16));
            o.setByteOrder(IotModbusByteOrderEnum.AB.getOrder());
            o.setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType());
            o.setScale(randomScale());
        };
        return randomPojo(IotDeviceModbusPointDO.class, ArrayUtils.append(consumer, consumers));
    }

    /**
     * 随机保存请求：默认是位点位（FC03、BOOLEAN、1 个寄存器，位索引 0-15）
     */
    @SafeVarargs
    private static IotDeviceModbusPointSaveReqVO randomSaveReqVO(Consumer<IotDeviceModbusPointSaveReqVO>... consumers) {
        Consumer<IotDeviceModbusPointSaveReqVO> consumer = (o) -> {
            o.setFunctionCode(IotModbusFunctionCodeEnum.READ_HOLDING_REGISTERS.getCode());
            o.setRegisterCount(1);
            o.setBitIndex(RandomUtil.randomInt(0, 16));
            o.setByteOrder(IotModbusByteOrderEnum.AB.getOrder());
            o.setRawDataType(IotModbusRawDataTypeEnum.BOOLEAN.getType());
            o.setScale(randomScale());
        };
        return randomPojo(IotDeviceModbusPointSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

    /**
     * 随机缩放因子：保留 6 位小数，与 H2 的 decimal(20, 6) 一致，读回后可以直接比对
     */
    private static BigDecimal randomScale() {
        return BigDecimal.valueOf(RandomUtil.randomInt(1, 1000000), 6);
    }

}
