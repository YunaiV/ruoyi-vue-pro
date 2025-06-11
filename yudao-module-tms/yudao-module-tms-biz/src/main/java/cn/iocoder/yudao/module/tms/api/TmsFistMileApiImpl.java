package cn.iocoder.yudao.module.tms.api;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.api.first.mile.TmsFistMileApi;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileItemUpdateDTO;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileUpdateDTO;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsOutboundItemReqDTO;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsOutboundReqDTO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.service.first.mile.TmsFirstMileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
public class TmsFistMileApiImpl implements TmsFistMileApi {
    @Autowired
    @Lazy
    private TmsFirstMileService tmsFirstMileService;


    /**
     * 出库单审批通过回调
     * <p>
     * 修改头程单状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outboundAudit(TmsOutboundReqDTO dto) {
        // 1.0 校验上游类型是否是头程出库
        if (!Objects.equals(dto.getUpstreamType(), BillType.TMS_FIRST_MILE.getValue())) {
            throw new IllegalArgumentException(StrUtil.format("出库单审核回调TmsOutboundReqDTO，上游类型({})不是头程单", Objects.requireNonNull(BillType.parse(dto.getUpstreamType())).getLabel()));
        }

        // 2.0 校验reqDTO的items的upstreamItemId是否存在
        List<Long> itemIds = dto.getItems().stream()
            .map(TmsOutboundItemReqDTO::getUpstreamId)
            .toList();
        List<TmsFirstMileItemDO> items = tmsFirstMileService.validateFirstMileItemExists(itemIds);

        // 3.0 获取主单并更新出库时间、出库状态、出库单ID、出库单编码
        tmsFirstMileService.updateFirstMileStatus(new TmsFistMileUpdateDTO()
            .setId(items.get(0).getFirstMileId())
            .setOutboundTime(dto.getOutboundTime())
            .setOutboundStatus(dto.getOutboundStatus())
        );

        // 3.1 填充子项的出库明细数值
        for (TmsOutboundItemReqDTO outboundItem : dto.getItems()) {
            tmsFirstMileService.updateFirstMileItemOutbound(new TmsFistMileItemUpdateDTO()
                .setId(outboundItem.getUpstreamId())
                .setInOutType(false)
                .setOutboundId(outboundItem.getUpstreamId())
                .setOutboundQty(outboundItem.getActualQty().intValue())
            );
        }
    }
}
