package cn.iocoder.yudao.module.wms.service.inbound.item.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.WmsInboundItemFlowSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundItemFlowDetailVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.INBOUND_ITEM_FLOW_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.INBOUND_NOT_EXISTS;

/**
 * 入库单库存详情扣减 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsInboundItemFlowServiceImpl implements WmsItemFlowService {

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    private WmsInboundItemFlowMapper inboundItemFlowMapper;

    /**
     * @sign : 1F1F95A6107AA047
     */
    @Override
    public WmsItemFlowDO createInboundItemFlow(WmsInboundItemFlowSaveReqVO createReqVO) {
        // 按 wms_inbound_flow.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (createReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(createReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 插入
        WmsItemFlowDO inboundItemFlow = BeanUtils.toBean(createReqVO, WmsItemFlowDO.class);
        inboundItemFlowMapper.insert(inboundItemFlow);
        // 返回
        return inboundItemFlow;
    }

    /**
     * @sign : 8A5562532D453C4B
     */
    @Override
    public WmsItemFlowDO updateInboundItemFlow(WmsInboundItemFlowSaveReqVO updateReqVO) {
        // 校验存在
        WmsItemFlowDO exists = validateInboundItemFlowExists(updateReqVO.getId());
        // 按 wms_inbound_flow.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (updateReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(updateReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 更新
        WmsItemFlowDO inboundItemFlow = BeanUtils.toBean(updateReqVO, WmsItemFlowDO.class);
        inboundItemFlowMapper.updateById(inboundItemFlow);
        // 返回
        return inboundItemFlow;
    }

    /**
     * @sign : CD7AC16303CBA5CC
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInboundItemFlow(Long id) {
        // 校验存在
        WmsItemFlowDO inboundItemFlow = validateInboundItemFlowExists(id);
        // 删除
        inboundItemFlowMapper.deleteById(id);
    }

    /**
     * @sign : E718B98F68AFE635
     */
    private WmsItemFlowDO validateInboundItemFlowExists(Long id) {
        WmsItemFlowDO inboundItemFlow = inboundItemFlowMapper.selectById(id);
        if (inboundItemFlow == null) {
            throw exception(INBOUND_ITEM_FLOW_NOT_EXISTS);
        }
        return inboundItemFlow;
    }

    @Override
    public WmsItemFlowDO getInboundItemFlow(Long id) {
        return inboundItemFlowMapper.selectById(id);
    }

    @Override
    public PageResult<WmsItemFlowDO> getInboundItemFlowPage(WmsInboundItemFlowPageReqVO pageReqVO) {
        return inboundItemFlowMapper.selectPage(pageReqVO);
    }

    /**
     * 按 inboundId 查询 WmsItemFlowDO
     */
    @Override
    public List<WmsItemFlowDO> selectByInboundId(Long inboundId, int limit) {
        return inboundItemFlowMapper.selectByInboundId(inboundId, limit);
    }


    /**
     * 按 inboundIds 查询 List<WmsItemFlowDO>
     */
    @Override
    public List<WmsItemFlowDO> selectByInboundIds(List<Long> inboundIds, int limit) {
        return inboundItemFlowMapper.selectByInboundIds(inboundIds, limit);
    }

    @Override
    public void insert(WmsItemFlowDO flowDO) {
        inboundItemFlowMapper.insert(flowDO);
    }

    @Override
    public List<WmsItemFlowDO> selectByActionId(Long latestOutboundActionId) {
        return inboundItemFlowMapper.selectByOutboundActionId(latestOutboundActionId);
    }

    @Override
    public List<WmsItemFlowDO> selectByOutboundId(Long outboundId, Long productId) {
        return inboundItemFlowMapper.selectByOutboundId(outboundId, productId);
    }

    /**
     * 按 ID 集合查询 WmsItemFlowDO
     */
    @Override
    public List<WmsItemFlowDO> selectByIds(Set<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inboundItemFlowMapper.selectByIds(idList);
    }

    /**
     * 按 ID 集合查询 WmsItemFlowDO
     */
    @Override
    public List<WmsItemFlowDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inboundItemFlowMapper.selectByIds(idList);
    }

    @Override
    public List<WmsInboundItemFlowDetailVO> selectByProductIdAndBinIdAndWarehouseId(Long productId, Long binId, Long warehouseId, int limit) {
        return inboundItemFlowMapper.selectByProductIdAndBinIdAndWarehouseId(productId, binId, warehouseId, 1000);
    }

}
