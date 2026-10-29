package cn.iocoder.yudao.module.iot.api.device;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceAuthReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceGetReqDTO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusConfigService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceModbusPointService;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.aop.support.AopUtils;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IoTDeviceApiImpl} 的单元测试：经过真实的 Spring 方法校验代理（与 @Validated 的 Bean 在容器中一致）
 *
 * @author 芋道源码
 */
public class IoTDeviceApiImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private IoTDeviceApiImpl deviceApiImpl;

    @Mock
    private IotDeviceService deviceService;
    @Mock
    private IotProductService productService;
    @Mock
    private IotDeviceModbusConfigService modbusConfigService;
    @Mock
    private IotDeviceModbusPointService modbusPointService;

    /**
     * 方法校验代理：参数约束继承不合法时，代理上的任何方法调用都会抛 ConstraintDeclarationException（HV000151）
     */
    private IotDeviceCommonApi deviceApi;

    @BeforeEach
    public void setUp() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(Validation.buildDefaultValidatorFactory().getValidator());
        processor.afterPropertiesSet();
        deviceApi = (IotDeviceCommonApi) processor.postProcessAfterInitialization(deviceApiImpl, "ioTDeviceApiImpl");
        assertTrue(AopUtils.isAopProxy(deviceApi));
    }

    @Test
    public void testAuthDevice() {
        // 准备参数
        IotDeviceAuthReqDTO reqDTO = new IotDeviceAuthReqDTO("client", "dn&pk", "password");
        // mock 方法
        when(deviceService.authDevice(same(reqDTO))).thenReturn(true);

        // 调用
        CommonResult<Boolean> result = deviceApi.authDevice(reqDTO);
        // 断言：方法校验代理正常放行
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    @Test
    public void testGetDevice_scriptEnabled() {
        // 准备参数
        IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO().setId(10L);
        // mock 方法：产品启用数据解析脚本
        when(deviceService.getDeviceFromCache(eq(10L))).thenReturn(IotDeviceDO.builder().id(10L).productId(1L)
                .productKey("pk").deviceName("dn").build());
        when(productService.getProductFromCache(eq(1L))).thenReturn(IotProductDO.builder().id(1L)
                .protocolType("tcp").serializeType("json")
                .scriptEnabled(true).scriptContent("function decode(ctx) {}").build());

        // 调用
        CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
        // 断言：产品字段随设备信息返回，启用时带上脚本内容
        assertTrue(result.isSuccess());
        IotDeviceRespDTO device = result.getData();
        assertEquals(10L, device.getId());
        assertEquals(1L, device.getProductId());
        assertEquals("tcp", device.getProtocolType());
        assertEquals("json", device.getSerializeType());
        assertTrue(device.getScriptEnabled());
        assertEquals("function decode(ctx) {}", device.getScriptContent());
    }

    @Test
    public void testGetDevice_scriptDisabled() {
        // 准备参数
        IotDeviceGetReqDTO reqDTO = new IotDeviceGetReqDTO().setProductKey("pk").setDeviceName("dn");
        // mock 方法：产品停用数据解析脚本，但保留了脚本内容
        when(deviceService.getDeviceFromCache(eq("pk"), eq("dn"))).thenReturn(IotDeviceDO.builder().id(10L)
                .productId(1L).productKey("pk").deviceName("dn").build());
        when(productService.getProductFromCache(eq(1L))).thenReturn(IotProductDO.builder().id(1L)
                .scriptEnabled(false).scriptContent("function decode(ctx) {}").build());

        // 调用
        CommonResult<IotDeviceRespDTO> result = deviceApi.getDevice(reqDTO);
        // 断言：停用时不返回脚本内容
        assertTrue(result.isSuccess());
        assertFalse(result.getData().getScriptEnabled());
        assertNull(result.getData().getScriptContent());
    }

}
