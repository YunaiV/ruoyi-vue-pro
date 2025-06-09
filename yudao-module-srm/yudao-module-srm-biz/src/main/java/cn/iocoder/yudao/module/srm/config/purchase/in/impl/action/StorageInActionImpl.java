package cn.iocoder.yudao.module.srm.config.purchase.in.impl.action;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.module.srm.config.machine.in.SrmPurchaseInCountContext;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseInMapper;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class StorageInActionImpl implements Action<SrmStorageStatus, SrmEventEnum, SrmPurchaseInCountContext> {

    @Autowired
    private SrmPurchaseInMapper srmPurchaseInMapper;
    @Autowired
    private SrmPurchaseInItemMapper srmPurchaseInItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(SrmStorageStatus from, SrmStorageStatus to, SrmEventEnum event, SrmPurchaseInCountContext context) {
        // 1. 获取入库单信息
        SrmPurchaseInDO purchaseIn = srmPurchaseInMapper.selectById(context.getArriveId());
        if (purchaseIn == null) {
            log.error("[execute][入库单({}) 不存在]", context.getArriveId());
            return;
        }
        if (event == SrmEventEnum.STORAGE_INIT) {
            //初始化 to就够了
        }
        if (event == SrmEventEnum.STOCK_ADJUSTMENT) {
            // 2. 获取所有入库项
            List<SrmPurchaseInItemDO> items = srmPurchaseInItemMapper.selectListByInId(context.getArriveId());
            if (items.isEmpty()) {
                log.error("[execute][入库单({}) 没有入库项]", context.getArriveId());
                return;
            }

            // 3. 统计入库项状态
            int totalCount = items.size();
            int allInCount = 0; // 全部入库数量
            int partialInCount = 0; // 部分入库数量
            int notInCount = 0; // 未入库数量

            for (SrmPurchaseInItemDO item : items) {
                if (item.getInboundStatus() == null) {
                    notInCount++;
                    continue;
                }
                SrmStorageStatus itemStatus = SrmStorageStatus.fromCode(item.getInboundStatus());
                switch (itemStatus) {
                    case ALL_IN_STORAGE: // 全部入库
                        allInCount++;
                        break;
                    case PARTIALLY_IN_STORAGE: // 部分入库
                        partialInCount++;
                        break;
                    case NONE_IN_STORAGE: // 未入库
                        notInCount++;
                        break;
                    default:
                        log.warn("Unknown SrmStorageStatus item status: {},item ID:{}", itemStatus, item.getId());
                        break;
                }
            }

            // 4. 根据统计结果更新入库单状态
            if (allInCount == totalCount) {
                // 全部入库项都已入库
                to = SrmStorageStatus.ALL_IN_STORAGE;
            } else if (notInCount == totalCount) {
                // 全部入库项都未入库
                to = SrmStorageStatus.NONE_IN_STORAGE;
            } else {
                // 部分入库项已入库
                to = SrmStorageStatus.PARTIALLY_IN_STORAGE;
            }
        }


        // 5. 更新入库单状态
        if (!to.getCode().equals(purchaseIn.getInboundStatus())) {
            srmPurchaseInMapper.updateById(purchaseIn.setInboundStatus(to.getCode()));
        }
        log.info("[execute][入库单({}) 状态从({}) 更新为({})]",
            context.getArriveId(),
            SrmStorageStatus.getDescriptionByCode(purchaseIn.getInboundStatus()),
                to.getDesc());
    }
}
