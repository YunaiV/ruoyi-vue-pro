package cn.iocoder.yudao.module.tms.service.transfer.item;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.transfer.item.TmsTransferItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.TRANSFER_ITEM_NOT_EXISTS;


/**
 * 调拨单明细 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class TmsTransferItemServiceImpl implements TmsTransferItemService {

    @Resource
    private TmsTransferItemMapper transferItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransferItem(TmsTransferItemSaveReqVO createReqVO) {
        // 插入
        TmsTransferItemDO transferItem = BeanUtils.toBean(createReqVO, TmsTransferItemDO.class);
        transferItemMapper.insert(transferItem);
        // 返回
        return transferItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransferItem(TmsTransferItemSaveReqVO updateReqVO) {
        // 校验存在
        validateTransferItemExists(updateReqVO.getId());
        // 更新
        TmsTransferItemDO updateObj = BeanUtils.toBean(updateReqVO, TmsTransferItemDO.class);
        transferItemMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransferItem(Long id) {
        // 校验存在
        validateTransferItemExists(id);
        // 删除
        transferItemMapper.deleteById(id);
    }

    private void validateTransferItemExists(Long id) {
        if (transferItemMapper.selectById(id) == null) {
            throw exception(TRANSFER_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public List<TmsTransferItemDO> validateTransferItemExists(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        List<TmsTransferItemDO> items = transferItemMapper.selectByIds(new HashSet<>(ids));
        if (items.size() != ids.size()) {
            throw exception(TRANSFER_ITEM_NOT_EXISTS,
                CollUtil.subtract(ids, CollUtil.newArrayList(items.stream().map(TmsTransferItemDO::getId).collect(Collectors.toSet()))));
        }
        return items;
    }

    @Override
    public TmsTransferItemDO getTransferItem(Long id) {
        return transferItemMapper.selectById(id);
    }

    @Override
    public List<TmsTransferItemDO> getTransferItemListByTransferId(Long transferId) {
        return transferItemMapper.selectListByTransferId(transferId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TmsTransferItemDO> getTransferItemListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        return transferItemMapper.selectByIds(ids);
    }

    @Override
    public void createTransferItemList(List<TmsTransferItemDO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        transferItemMapper.insertBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransferItemList(List<TmsTransferItemDO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        transferItemMapper.updateBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransferItemList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        transferItemMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransferItemByTransferId(Long transferId) {
        transferItemMapper.deleteByTransferId(transferId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransferItemOutbound(Long id, Integer outboundQty) {
        // 1. 校验存在
        validateTransferItemExists(id);
        // 2. 更新出库信息
        transferItemMapper.updateById(new TmsTransferItemDO().setId(id).setOutboundClosedQty(outboundQty));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransferItemInbound(Long itemId, int i) {
        // 1. 校验存在
        validateTransferItemExists(itemId);
        //2.0 更新入库信息
        transferItemMapper.updateById(new TmsTransferItemDO().setId(itemId).setInboundClosedQty(i));
    }
}