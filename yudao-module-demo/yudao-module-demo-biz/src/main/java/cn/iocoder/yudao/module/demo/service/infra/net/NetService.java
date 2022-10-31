package cn.iocoder.yudao.module.demo.service.infra.net;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.net.NetDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 区块链网络 Service 接口
 *
 * @author ruanzh.eth
 */
public interface NetService {

    /**
     * 创建区块链网络
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createNet(@Valid NetCreateReqVO createReqVO);

    /**
     * 更新区块链网络
     *
     * @param updateReqVO 更新信息
     */
    void updateNet(@Valid NetUpdateReqVO updateReqVO);

    /**
     * 删除区块链网络
     *
     * @param id 编号
     */
    void deleteNet(Long id);

    /**
     * 获得区块链网络
     *
     * @param id 编号
     * @return 区块链网络
     */
    NetDO getNet(Long id);

    /**
     * 获得区块链网络列表
     *
     * @param ids 编号
     * @return 区块链网络列表
     */
    List<NetDO> getNetList(Collection<Long> ids);

    /**
     * 获得区块链网络分页
     *
     * @param pageReqVO 分页查询
     * @return 区块链网络分页
     */
    PageResult<NetDO> getNetPage(NetPageReqVO pageReqVO);

    /**
     * 获得区块链网络列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 区块链网络列表
     */
    List<NetDO> getNetList(NetExportReqVO exportReqVO);

}
