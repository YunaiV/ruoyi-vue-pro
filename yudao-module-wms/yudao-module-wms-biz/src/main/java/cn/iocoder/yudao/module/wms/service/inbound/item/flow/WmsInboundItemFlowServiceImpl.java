package cn.iocoder.yudao.module.wms.service.inbound.item.flow;

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

/**
 * 入库单库存详情扣减 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInboundItemFlowServiceImpl implements WmsInboundItemFlowService {

    @Resource
    private WmsInboundItemFlowMapper inboundItemFlowMapper;

    @Override
    public Long createInboundItemFlow(WmsInboundItemFlowSaveReqVO createReqVO) {
        // 插入
        WmsInboundItemFlowDO inboundItemFlow = BeanUtils.toBean(createReqVO, WmsInboundItemFlowDO.class);
        inboundItemFlowMapper.insert(inboundItemFlow);
        // 返回
        return inboundItemFlow.getId();
    }

    @Override
    public void updateInboundItemFlow(WmsInboundItemFlowSaveReqVO updateReqVO) {
        // 校验存在
        validateInboundItemFlowExists(updateReqVO.getId());
        // 更新
        WmsInboundItemFlowDO updateObj = BeanUtils.toBean(updateReqVO, WmsInboundItemFlowDO.class);
        inboundItemFlowMapper.updateById(updateObj);
    }

    @Override
    public void deleteInboundItemFlow(Long id) {
        // 校验存在
        validateInboundItemFlowExists(id);
        // 删除
        inboundItemFlowMapper.deleteById(id);
    }

    private void validateInboundItemFlowExists(Long id) {
        if (inboundItemFlowMapper.selectById(id) == null) {
            //throw exception(INBOUND_ITEM_FLOW_NOT_EXISTS);
        }
    }

    @Override
    public WmsInboundItemFlowDO getInboundItemFlow(Long id) {
        return inboundItemFlowMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundItemFlowDO> getInboundItemFlowPage(WmsInboundItemFlowPageReqVO pageReqVO) {
        return inboundItemFlowMapper.selectPage(pageReqVO);
    }

}