package cn.iocoder.yudao.module.wms.service.inbound.item.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

/**
 * 入库单库存详情扣减 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInboundItemFlowService {

    /**
     * 创建入库单库存详情扣减
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsInboundItemFlowDO createInboundItemFlow(@Valid WmsInboundItemFlowSaveReqVO createReqVO);

    /**
     * 更新入库单库存详情扣减
     *
     * @param updateReqVO 更新信息
     */
    WmsInboundItemFlowDO updateInboundItemFlow(@Valid WmsInboundItemFlowSaveReqVO updateReqVO);

    /**
     * 删除入库单库存详情扣减
     *
     * @param id 编号
     */
    void deleteInboundItemFlow(Long id);

    /**
     * 获得入库单库存详情扣减
     *
     * @param id 编号
     * @return 入库单库存详情扣减
     */
    WmsInboundItemFlowDO getInboundItemFlow(Long id);

    /**
     * 获得入库单库存详情扣减分页
     *
     * @param pageReqVO 分页查询
     * @return 入库单库存详情扣减分页
     */
    PageResult<WmsInboundItemFlowDO> getInboundItemFlowPage(WmsInboundItemFlowPageReqVO pageReqVO);

    /**
     * 按 inboundId 查询 WmsInboundItemFlowDO
     */
    List<WmsInboundItemFlowDO> selectByInboundId(Long inboundId, int limit);

    void insert(WmsInboundItemFlowDO flowDO);

    List<WmsInboundItemFlowDO> selectByActionId(Long latestOutboundActionId);

    /**
     * 按 outboundId 查询 WmsInboundItemFlowDO
     *
     * @param outboundId 出库单 ID
     * @param productId  产品 ID
     * @return 入库单库存详情扣减列表
     */
    List<WmsInboundItemFlowDO> selectByOutboundId(Long outboundId, Long productId);

    /**
     * 按 ID 集合查询 WmsInboundItemFlowDO
     */
    List<WmsInboundItemFlowDO> selectByIds(Set<Long> idList);

    /**
     * 按 ID 集合查询 WmsInboundItemFlowDO
     */
    List<WmsInboundItemFlowDO> selectByIds(List<Long> idList);
}
