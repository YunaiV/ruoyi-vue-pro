package cn.iocoder.yudao.module.iot.service.opcuaserver;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver.IotOpcUaServerConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver.IotOpcUaServerNodeMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotOpcUaServerConfigServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(IotOpcUaServerConfigServiceImpl.class)
public class IotOpcUaServerConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private IotOpcUaServerConfigServiceImpl opcUaServerConfigService;

    @Resource
    private IotOpcUaServerConfigMapper opcUaServerConfigMapper;
    @Resource
    private IotOpcUaServerNodeMapper opcUaServerNodeMapper;

    @Test
    public void testCreateOpcUaServerConfig_success() {
        // 准备参数
        IotOpcUaServerConfigSaveReqVO reqVO = newSaveReqVO("scada-a");

        // 调用
        Long id = opcUaServerConfigService.createOpcUaServerConfig(reqVO);
        // 断言：只保存凭据别名，没有口令字段
        IotOpcUaServerConfigDO config = opcUaServerConfigMapper.selectById(id);
        assertEquals("scada-a", config.getCredentialAlias());
        assertEquals(2, config.getMaxSessions());
        assertEquals(5, config.getMaxSubscriptionsPerSession());
        assertEquals(1000, config.getMaxMonitoredItemsPerSession());
    }

    @Test
    public void testCreateOpcUaServerConfig_credentialAliasExistsInOtherTenant() {
        // mock 数据：其它租户已使用该凭据别名
        IotOpcUaServerConfigDO other = IotOpcUaServerConfigDO.builder().name("other").credentialAlias("scada-a")
                .maxSessions(2).maxSubscriptionsPerSession(5).maxMonitoredItemsPerSession(1000)
                .status(CommonStatusEnum.ENABLE.getStatus()).build();
        other.setTenantId(2L);
        opcUaServerConfigMapper.insert(other);

        // 调用，并断言：凭据别名跨租户唯一
        assertServiceException(() -> opcUaServerConfigService.createOpcUaServerConfig(newSaveReqVO("scada-a")),
                OPC_UA_SERVER_CONFIG_CREDENTIAL_ALIAS_EXISTS);
    }

    @Test
    public void testUpdateOpcUaServerConfig_notExists() {
        IotOpcUaServerConfigSaveReqVO reqVO = newSaveReqVO("scada-a");
        reqVO.setId(404L);
        assertServiceException(() -> opcUaServerConfigService.updateOpcUaServerConfig(reqVO),
                OPC_UA_SERVER_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteOpcUaServerConfig_hasNodes() {
        // mock 数据：配置下有节点
        Long id = opcUaServerConfigService.createOpcUaServerConfig(newSaveReqVO("scada-a"));
        opcUaServerNodeMapper.insert(IotOpcUaServerNodeDO.builder().configId(id).deviceId(1L).thingModelId(1L)
                .browseName("temperature").status(CommonStatusEnum.ENABLE.getStatus()).build());

        // 调用，并断言：有节点时不能删除
        assertServiceException(() -> opcUaServerConfigService.deleteOpcUaServerConfig(id), OPC_UA_SERVER_CONFIG_HAS_NODES);
    }

    @Test
    public void testDeleteOpcUaServerConfig_success() {
        Long id = opcUaServerConfigService.createOpcUaServerConfig(newSaveReqVO("scada-a"));
        opcUaServerConfigService.deleteOpcUaServerConfig(id);
        assertNull(opcUaServerConfigMapper.selectById(id));
    }

    @Test
    public void testGetEnabledOpcUaServerConfigList() {
        // mock 数据：启用（租户 1）、禁用（租户 1）、启用（租户 2）
        insertConfig("a", CommonStatusEnum.ENABLE.getStatus(), 1L);
        insertConfig("b", CommonStatusEnum.DISABLE.getStatus(), 1L);
        insertConfig("c", CommonStatusEnum.ENABLE.getStatus(), 2L);

        // 断言：返回全部租户启用的，按编号升序
        List<IotOpcUaServerConfigDO> list = opcUaServerConfigService.getEnabledOpcUaServerConfigList();
        assertEquals(2, list.size());
        assertEquals("a", list.get(0).getCredentialAlias());
        assertEquals("c", list.get(1).getCredentialAlias());
    }

    private void insertConfig(String alias, Integer status, Long tenantId) {
        IotOpcUaServerConfigDO config = IotOpcUaServerConfigDO.builder().name(alias).credentialAlias(alias)
                .maxSessions(2).maxSubscriptionsPerSession(5).maxMonitoredItemsPerSession(1000).status(status).build();
        config.setTenantId(tenantId);
        opcUaServerConfigMapper.insert(config);
    }

    private static IotOpcUaServerConfigSaveReqVO newSaveReqVO(String alias) {
        IotOpcUaServerConfigSaveReqVO reqVO = new IotOpcUaServerConfigSaveReqVO();
        reqVO.setName("一号车间");
        reqVO.setCredentialAlias(alias);
        reqVO.setMaxSessions(2);
        reqVO.setMaxSubscriptionsPerSession(5);
        reqVO.setMaxMonitoredItemsPerSession(1000);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

}
