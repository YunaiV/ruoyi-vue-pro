package cn.iocoder.yudao.module.tms.convert.transfer;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import cn.iocoder.yudao.module.tms.service.bo.transfer.TmsTransferBO;
import cn.iocoder.yudao.module.tms.service.bo.transfer.TmsTransferItemBO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.TRANSFER_NOT_EXISTS;

/**
 * 调拨单 Convert
 *
 * @author wdy
 */
public class TmsTransferConvert {

    /**
     * 将明细 BO 列表转换为 BO 列表
     *
     * @param itemBOList 明细 BO 列表
     * @return BO 列表
     */
    public static List<TmsTransferBO> convertBOList(List<TmsTransferItemBO> itemBOList) {
        return itemBOList.stream()
            .collect(Collectors.groupingBy(TmsTransferItemBO::getTransferId))
            .values().stream()
            .map(itemBOs -> {
                // 获取第一个明细项的主表信息（同一主表ID下的所有明细项，其主表信息相同）
                TmsTransferItemBO firstItem = itemBOs.stream().findFirst().orElseThrow(() ->
                    exception(TRANSFER_NOT_EXISTS));
                // 降序排序
                itemBOs.sort(Comparator.comparing(TmsTransferItemBO::getCreateTime).reversed());
                return BeanUtils.toBean(firstItem.getTmsTransferDO(), TmsTransferBO.class, bo -> {
                    bo.setTmsTransferItemDOList(BeanUtils.toBean(itemBOs, TmsTransferItemDO.class));
                });
            })
            .sorted(Comparator.comparing(TmsTransferBO::getCreateTime).reversed())
            .collect(Collectors.toList());
    }
} 