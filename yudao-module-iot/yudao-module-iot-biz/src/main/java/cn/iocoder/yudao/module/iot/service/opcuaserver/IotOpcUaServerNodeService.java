package cn.iocoder.yudao.module.iot.service.opcuaserver;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import javax.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * IoT OPC UA Server 节点 Service 接口
 *
 * @author 芋道源码
 */
public interface IotOpcUaServerNodeService {

    /**
     * 创建 OPC UA Server 节点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOpcUaServerNode(@Valid IotOpcUaServerNodeSaveReqVO createReqVO);

    /**
     * 更新 OPC UA Server 节点
     *
     * @param updateReqVO 更新信息
     */
    void updateOpcUaServerNode(@Valid IotOpcUaServerNodeSaveReqVO updateReqVO);

    /**
     * 删除 OPC UA Server 节点
     *
     * @param id 编号
     */
    void deleteOpcUaServerNode(Long id);

    /**
     * 获得 OPC UA Server 节点
     *
     * @param id 编号
     * @return OPC UA Server 节点
     */
    IotOpcUaServerNodeDO getOpcUaServerNode(Long id);

    /**
     * 获得 OPC UA Server 节点分页
     *
     * @param pageReqVO 分页查询
     * @return OPC UA Server 节点分页
     */
    PageResult<IotOpcUaServerNodeDO> getOpcUaServerNodePage(IotOpcUaServerNodePageReqVO pageReqVO);

    /**
     * 获得启用的节点列表，按配置编号分组
     *
     * @param configIds 配置编号集合
     * @return 配置编号 -> 节点列表
     */
    Map<Long, List<IotOpcUaServerNodeDO>> getEnabledOpcUaServerNodeMapByConfigIds(Collection<Long> configIds);

    /**
     * 获得产品的属性物模型
     *
     * @param productIds 产品编号集合
     * @return 物模型编号 -> 物模型
     */
    Map<Long, IotThingModelDO> getPropertyThingModelMap(Collection<Long> productIds);

}
