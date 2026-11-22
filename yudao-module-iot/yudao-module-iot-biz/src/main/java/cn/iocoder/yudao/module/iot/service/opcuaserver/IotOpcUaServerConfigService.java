package cn.iocoder.yudao.module.iot.service.opcuaserver;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT OPC UA Server 配置 Service 接口
 *
 * @author 芋道源码
 */
public interface IotOpcUaServerConfigService {

    /**
     * 创建 OPC UA Server 配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpcUaServerConfig(@Valid IotOpcUaServerConfigSaveReqVO createReqVO);

    /**
     * 更新 OPC UA Server 配置
     *
     * @param updateReqVO 更新信息
     */
    void updateOpcUaServerConfig(@Valid IotOpcUaServerConfigSaveReqVO updateReqVO);

    /**
     * 删除 OPC UA Server 配置：配置下还有节点时不允许删除
     *
     * @param id 编号
     */
    void deleteOpcUaServerConfig(Long id);

    /**
     * 校验 OPC UA Server 配置存在
     *
     * @param id 编号
     * @return OPC UA Server 配置
     */
    IotOpcUaServerConfigDO validateOpcUaServerConfigExists(Long id);

    /**
     * 获得 OPC UA Server 配置
     *
     * @param id 编号
     * @return OPC UA Server 配置
     */
    IotOpcUaServerConfigDO getOpcUaServerConfig(Long id);

    /**
     * 获得 OPC UA Server 配置分页
     *
     * @param pageReqVO 分页查询
     * @return OPC UA Server 配置分页
     */
    PageResult<IotOpcUaServerConfigDO> getOpcUaServerConfigPage(IotOpcUaServerConfigPageReqVO pageReqVO);

    /**
     * 获得全部租户启用的 OPC UA Server 配置列表：供网关调用，调用方负责忽略租户
     *
     * @return OPC UA Server 配置列表
     */
    List<IotOpcUaServerConfigDO> getEnabledOpcUaServerConfigList();

}
