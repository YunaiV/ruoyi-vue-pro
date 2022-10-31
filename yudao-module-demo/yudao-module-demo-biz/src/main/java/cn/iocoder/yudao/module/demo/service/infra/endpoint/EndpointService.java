package cn.iocoder.yudao.module.demo.service.infra.endpoint;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint.EndpointDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 区块链节点 Service 接口
 *
 * @author ruanzh.eth
 */
public interface EndpointService {

    /**
     * 创建区块链节点
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEndpoint(@Valid EndpointCreateReqVO createReqVO);

    /**
     * 更新区块链节点
     *
     * @param updateReqVO 更新信息
     */
    void updateEndpoint(@Valid EndpointUpdateReqVO updateReqVO);

    /**
     * 删除区块链节点
     *
     * @param id 编号
     */
    void deleteEndpoint(Long id);

    /**
     * 获得区块链节点
     *
     * @param id 编号
     * @return 区块链节点
     */
    EndpointDO getEndpoint(Long id);

    /**
     * 获得区块链节点列表
     *
     * @param ids 编号
     * @return 区块链节点列表
     */
    List<EndpointDO> getEndpointList(Collection<Long> ids);

    /**
     * 获得区块链节点分页
     *
     * @param pageReqVO 分页查询
     * @return 区块链节点分页
     */
    PageResult<EndpointDO> getEndpointPage(EndpointPageReqVO pageReqVO);

    /**
     * 获得区块链节点列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 区块链节点列表
     */
    List<EndpointDO> getEndpointList(EndpointExportReqVO exportReqVO);

}
