package cn.iocoder.yudao.module.iot.service.device;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua.IotDeviceOpcUaConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigListReqDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaAuthTypeEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceOpcUaConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.device.IotDeviceOpcUaConfigMapper;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import javax.annotation.Resource;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link IotDeviceOpcUaConfigServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import({IotDeviceOpcUaConfigServiceImpl.class, ValidationAutoConfiguration.class})
public class IotDeviceOpcUaConfigServiceImplTest extends BaseDbUnitTest {

    private static final Integer AUTH_ANONYMOUS = IotOpcUaAuthTypeEnum.ANONYMOUS.getType();
    private static final Integer AUTH_USERNAME = IotOpcUaAuthTypeEnum.USERNAME.getType();

    @Resource
    private IotDeviceOpcUaConfigServiceImpl opcUaConfigService;

    @Resource
    private IotDeviceOpcUaConfigMapper opcUaConfigMapper;

    @MockBean
    private IotDeviceService deviceService;
    @MockBean
    private IotProductService productService;

    @Test
    public void testSaveDeviceOpcUaConfig_createAndUpdate() {
        // 准备参数
        IotDeviceOpcUaConfigSaveReqVO reqVO = newSaveReqVO();
        mockDevice(reqVO.getDeviceId(), IotProtocolTypeEnum.OPC_UA_CLIENT.getType());

        // 调用：新增
        opcUaConfigService.saveDeviceOpcUaConfig(reqVO);
        // 断言
        IotDeviceOpcUaConfigDO config = opcUaConfigMapper.selectByDeviceId(reqVO.getDeviceId());
        assertPojoEquals(reqVO, config, "id");
        assertEquals("opc.tcp://192.168.1.10:4840/server", config.getEndpointUrl());
        assertEquals(AUTH_USERNAME, config.getAuthType());
        assertEquals("plc-reader", config.getCredentialAlias());

        // 调用：再次保存为更新（每台设备一份配置）
        IotDeviceOpcUaConfigSaveReqVO updateReqVO = newSaveReqVO().setDeviceId(reqVO.getDeviceId())
                .setCredentialAlias("plc-writer").setTimeout(8000);
        opcUaConfigService.saveDeviceOpcUaConfig(updateReqVO);
        // 断言
        List<IotDeviceOpcUaConfigDO> list = opcUaConfigMapper.selectList();
        assertEquals(1, list.size());
        assertEquals(config.getId(), list.get(0).getId());
        assertEquals("plc-writer", list.get(0).getCredentialAlias());
        assertEquals(8000, list.get(0).getTimeout());
    }

    @Test
    public void testSaveDeviceOpcUaConfig_protocolInvalid() {
        IotDeviceOpcUaConfigSaveReqVO reqVO = newSaveReqVO();
        mockDevice(reqVO.getDeviceId(), IotProtocolTypeEnum.MODBUS_TCP_CLIENT.getType());

        assertServiceException(() -> opcUaConfigService.saveDeviceOpcUaConfig(reqVO),
                DEVICE_OPC_UA_CONFIG_PROTOCOL_INVALID);
    }

    @Test
    public void testSaveDeviceOpcUaConfig_endpointInvalid() {
        for (String endpointUrl : Arrays.asList("http://192.168.1.10:4840", "opc.tcp://192.168.1.10",
                "opc.tcp://user:pass@192.168.1.10:4840", "opc.tcp://192.168.1.10:70000", "not a url", " ",
                "opc.tcp://192.168.1.10:4840/server?x=1", "opc.tcp://192.168.1.10:4840/server#a",
                " opc.tcp://192.168.1.10:4840/server ")) { // 首尾空白：拒绝，不自动去掉
            IotDeviceOpcUaConfigSaveReqVO reqVO = newSaveReqVO().setEndpointUrl(endpointUrl);

            // 调用，并断言异常：参数校验不通过，不进入业务校验
            assertThrows(ConstraintViolationException.class, () -> opcUaConfigService.saveDeviceOpcUaConfig(reqVO),
                    endpointUrl);
        }
        verifyNoInteractions(deviceService);
    }

    @Test
    public void testSaveDeviceOpcUaConfig_securityInvalid() {
        // 只支持用户名认证，口令必须加密传输：None 策略、None / Sign 模式都不允许保存
        for (String[] security : new String[][]{{"None", "None"}, {"None", "Sign"}, {"None", "SignAndEncrypt"},
                {"Basic256Sha256", "None"}, {"Basic256Sha256", "Sign"}}) {
            assertThrows(ConstraintViolationException.class, () -> opcUaConfigService.saveDeviceOpcUaConfig(
                    newSaveReqVO().setSecurityPolicy(security[0]).setSecurityMode(security[1])),
                    security[0] + "/" + security[1]);
        }
        verifyNoInteractions(deviceService);
    }

    @Test
    public void testSaveDeviceOpcUaConfig_authInvalid() {
        // 只支持用户名认证：匿名不允许保存
        assertThrows(ConstraintViolationException.class, () -> opcUaConfigService.saveDeviceOpcUaConfig(
                newSaveReqVO().setAuthType(AUTH_ANONYMOUS).setCredentialAlias("")));
        // 用户名认证：必须填写凭据别名
        assertThrows(ConstraintViolationException.class, () -> opcUaConfigService.saveDeviceOpcUaConfig(
                newSaveReqVO().setCredentialAlias(" ")));
        verifyNoInteractions(deviceService);
    }

    @Test
    public void testGetDeviceOpcUaConfigList() {
        // mock 数据
        IotDeviceOpcUaConfigDO enabled = newConfigDO(1L, CommonStatusEnum.ENABLE.getStatus());
        opcUaConfigMapper.insert(enabled);
        opcUaConfigMapper.insert(newConfigDO(2L, CommonStatusEnum.DISABLE.getStatus()));
        IotDeviceOpcUaConfigDO deleted = newConfigDO(3L, CommonStatusEnum.ENABLE.getStatus());
        opcUaConfigMapper.insert(deleted);
        opcUaConfigMapper.deleteById(deleted.getId());

        // 调用
        List<IotDeviceOpcUaConfigDO> list = opcUaConfigService.getDeviceOpcUaConfigList(
                new IotOpcUaDeviceConfigListReqDTO().setStatus(CommonStatusEnum.ENABLE.getStatus()));

        // 断言：只返回启用、未删除的配置
        assertEquals(1, list.size());
        assertEquals(enabled.getId(), list.get(0).getId());
        assertNull(opcUaConfigService.getDeviceOpcUaConfigByDeviceId(3L));
    }

    @Test
    public void testSaveDeviceOpcUaConfig_toleranceDuplicate() {
        // mock 数据：同一设备已有两条未删除的配置（应用层唯一不加锁，并发保存可能产生）
        IotDeviceOpcUaConfigSaveReqVO reqVO = newSaveReqVO();
        mockDevice(reqVO.getDeviceId(), IotProtocolTypeEnum.OPC_UA_CLIENT.getType());
        IotDeviceOpcUaConfigDO first = BeanUtils.toBean(reqVO, IotDeviceOpcUaConfigDO.class);
        opcUaConfigMapper.insert(first);
        IotDeviceOpcUaConfigDO second = BeanUtils.toBean(reqVO, IotDeviceOpcUaConfigDO.class);
        opcUaConfigMapper.insert(second);

        // 调用：查询、保存都不抛 TooManyResultsException，按编号最小的一条处理
        assertEquals(first.getId(), opcUaConfigService.getDeviceOpcUaConfigByDeviceId(reqVO.getDeviceId()).getId());
        opcUaConfigService.saveDeviceOpcUaConfig(newSaveReqVO().setDeviceId(reqVO.getDeviceId()).setTimeout(9000));
        // 断言：更新第一条
        assertEquals(9000, opcUaConfigMapper.selectById(first.getId()).getTimeout());
        assertEquals(5000, opcUaConfigMapper.selectById(second.getId()).getTimeout());
    }

    // ========== 工具方法 ==========

    private void mockDevice(Long deviceId, String protocolType) {
        Long productId = randomLongId();
        when(deviceService.validateDeviceExists(eq(deviceId)))
                .thenReturn(IotDeviceDO.builder().id(deviceId).productId(productId).build());
        when(productService.getProduct(eq(productId)))
                .thenReturn(IotProductDO.builder().id(productId).protocolType(protocolType).build());
    }

    private static IotDeviceOpcUaConfigSaveReqVO newSaveReqVO() {
        return new IotDeviceOpcUaConfigSaveReqVO().setDeviceId(randomLongId())
                .setEndpointUrl("opc.tcp://192.168.1.10:4840/server")
                .setSecurityPolicy("Basic256Sha256").setSecurityMode("SignAndEncrypt")
                .setAuthType(AUTH_USERNAME).setCredentialAlias("plc-reader")
                .setTimeout(5000).setRetryInterval(10000).setStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private static IotDeviceOpcUaConfigDO newConfigDO(Long deviceId, Integer status) {
        return IotDeviceOpcUaConfigDO.builder().deviceId(deviceId)
                .endpointUrl("opc.tcp://192.168.1.10:4840/server")
                .securityPolicy("Basic256Sha256").securityMode("SignAndEncrypt")
                .authType(AUTH_USERNAME).credentialAlias("plc-reader")
                .timeout(5000).retryInterval(10000).status(status).build();
    }

}
