package cn.iocoder.yudao.module.wms.service.inbound.item.flow;

import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;
import java.util.List;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

/**
 * 入库单库存详情扣减 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsInboundItemFlowServiceImpl implements WmsInboundItemFlowService {

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    private WmsInboundItemFlowMapper inboundItemFlowMapper;

    /**
     * @sign : 1F1F95A6107AA047
     */
    @Override
    public WmsInboundItemFlowDO createInboundItemFlow(WmsInboundItemFlowSaveReqVO createReqVO) {
        // 按 wms_inbound_item_flow.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (createReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(createReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 插入
        WmsInboundItemFlowDO inboundItemFlow = BeanUtils.toBean(createReqVO, WmsInboundItemFlowDO.class);
        inboundItemFlowMapper.insert(inboundItemFlow);
        // 返回
        return inboundItemFlow;
    }

    /**
     * @sign : 8A5562532D453C4B
     */
    @Override
    public WmsInboundItemFlowDO updateInboundItemFlow(WmsInboundItemFlowSaveReqVO updateReqVO) {
        // 校验存在
        WmsInboundItemFlowDO exists = validateInboundItemFlowExists(updateReqVO.getId());
        // 按 wms_inbound_item_flow.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (updateReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(updateReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 更新
        WmsInboundItemFlowDO inboundItemFlow = BeanUtils.toBean(updateReqVO, WmsInboundItemFlowDO.class);
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
        WmsInboundItemFlowDO inboundItemFlow = validateInboundItemFlowExists(id);
        // 删除
        inboundItemFlowMapper.deleteById(id);
    }

    /**
     * @sign : E718B98F68AFE635
     */
    private WmsInboundItemFlowDO validateInboundItemFlowExists(Long id) {
        WmsInboundItemFlowDO inboundItemFlow = inboundItemFlowMapper.selectById(id);
        if (inboundItemFlow == null) {
            throw exception(INBOUND_ITEM_FLOW_NOT_EXISTS);
        }
        return inboundItemFlow;
    }

    @Override
    public WmsInboundItemFlowDO getInboundItemFlow(Long id) {
        return inboundItemFlowMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundItemFlowDO> getInboundItemFlowPage(WmsInboundItemFlowPageReqVO pageReqVO) {
        return inboundItemFlowMapper.selectPage(pageReqVO);
    }

    /**
     * 按 inboundId 查询 WmsInboundItemFlowDO
     */
    public List<WmsInboundItemFlowDO> selectByInboundId(Long inboundId, int limit) {
        return inboundItemFlowMapper.selectByInboundId(inboundId, limit);
    }

    @Override
    public void insert(WmsInboundItemFlowDO flowDO) {
        inboundItemFlowMapper.insert(flowDO);
    }

    @Override
    public List<WmsInboundItemFlowDO> selectByActionId(Long latestOutboundActionId) {
        return inboundItemFlowMapper.selectByOutboundActionId(latestOutboundActionId);
    }

    /**
     * 按 ID 集合查询 WmsInboundItemFlowDO
     */
    public List<WmsInboundItemFlowDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inboundItemFlowMapper.selectByIds(idList);
    }
}