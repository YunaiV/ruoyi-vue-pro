package cn.iocoder.yudao.module.iot.service.opcuaserver;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver.IotOpcUaServerConfigMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.opcuaserver.IotOpcUaServerNodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT OPC UA Server 配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotOpcUaServerConfigServiceImpl implements IotOpcUaServerConfigService {

    @Resource
    private IotOpcUaServerConfigMapper opcUaServerConfigMapper;
    @Resource
    private IotOpcUaServerNodeMapper opcUaServerNodeMapper;

    @Override
    public Long createOpcUaServerConfig(IotOpcUaServerConfigSaveReqVO createReqVO) {
        // 1. 校验凭据别名全局唯一（格式由 IotOpcUaServerConfigSaveReqVO 校验，不含空白）
        validateCredentialAliasUnique(null, createReqVO.getCredentialAlias());

        // 2. 插入
        IotOpcUaServerConfigDO config = BeanUtils.toBean(createReqVO, IotOpcUaServerConfigDO.class, o -> o.setId(null));
        opcUaServerConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateOpcUaServerConfig(IotOpcUaServerConfigSaveReqVO updateReqVO) {
        // 1. 校验存在、凭据别名全局唯一
        validateOpcUaServerConfigExists(updateReqVO.getId());
        validateCredentialAliasUnique(updateReqVO.getId(), updateReqVO.getCredentialAlias());

        // 2. 更新
        opcUaServerConfigMapper.updateById(BeanUtils.toBean(updateReqVO, IotOpcUaServerConfigDO.class));
    }

    @Override
    public void deleteOpcUaServerConfig(Long id) {
        // 1. 校验存在，且没有节点
        validateOpcUaServerConfigExists(id);
        if (opcUaServerNodeMapper.selectCountByConfigId(id) > 0) {
            throw exception(OPC_UA_SERVER_CONFIG_HAS_NODES);
        }
        // 2. 删除（逻辑删除）
        opcUaServerConfigMapper.deleteById(id);
    }

    /**
     * 校验凭据别名全局唯一：网关按登录账号对应的凭据别名定位配置（进而定位租户），所以需要跨租户唯一
     * <p>
     * 与 OPC UA Client 一致只做应用层校验不加锁；并发产生的重复别名，网关按编号最小的一条生效
     */
    private void validateCredentialAliasUnique(Long id, String credentialAlias) {
        IotOpcUaServerConfigDO config = TenantUtils.executeIgnore(
                () -> opcUaServerConfigMapper.selectByCredentialAlias(credentialAlias));
        if (config != null && ObjUtil.notEqual(config.getId(), id)) {
            throw exception(OPC_UA_SERVER_CONFIG_CREDENTIAL_ALIAS_EXISTS);
        }
    }

    @Override
    public IotOpcUaServerConfigDO validateOpcUaServerConfigExists(Long id) {
        IotOpcUaServerConfigDO config = opcUaServerConfigMapper.selectById(id);
        if (config == null) {
            throw exception(OPC_UA_SERVER_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    @Override
    public IotOpcUaServerConfigDO getOpcUaServerConfig(Long id) {
        return opcUaServerConfigMapper.selectById(id);
    }

    @Override
    public PageResult<IotOpcUaServerConfigDO> getOpcUaServerConfigPage(IotOpcUaServerConfigPageReqVO pageReqVO) {
        return opcUaServerConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotOpcUaServerConfigDO> getEnabledOpcUaServerConfigList() {
        return opcUaServerConfigMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
