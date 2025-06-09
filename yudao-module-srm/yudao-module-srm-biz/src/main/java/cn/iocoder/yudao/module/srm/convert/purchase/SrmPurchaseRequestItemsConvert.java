package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.request.SrmPurchaseRequestBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.request.SrmPurchaseRequestItemsBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购申请项->采购订单的订单项
 */
@Mapper
public interface SrmPurchaseRequestItemsConvert {
    SrmPurchaseRequestItemsConvert INSTANCE = Mappers.getMapper(SrmPurchaseRequestItemsConvert.class);

    /**
     * 将采购申请项 BO 列表转换为采购申请 BO 列表（带子项）
     *
     * @param itemsBOList 采购申请项 BO 列表，每项中包含其所属的采购申请主表信息
     * @return 采购申请 BO 列表，每个 BO 包含子项列表
     */
    default List<SrmPurchaseRequestBO> convertList(List<SrmPurchaseRequestItemsBO> itemsBOList) {
        // 如果输入为空，返回空列表
        if (CollUtil.isEmpty(itemsBOList)) {
            return Collections.emptyList();
        }

        // 按采购申请主表 ID 分组，聚合对应的子项列表
        Map<Long, List<SrmPurchaseRequestItemsBO>> itemsMap = itemsBOList.stream().collect(Collectors.groupingBy(SrmPurchaseRequestItemsBO::getRequestId));

        //构建主表 BO，并设置其子项列表
        return itemsMap.values().stream().map(srmPurchaseRequestItemsBOS -> {
            // 获取该组的第一项（代表当前主表的基本信息）
            SrmPurchaseRequestItemsBO firstItem = srmPurchaseRequestItemsBOS.get(0);

            // 将主表 DO 转为 BO
            SrmPurchaseRequestBO requestBO = BeanUtils.toBean(firstItem.getPurchaseRequest(), SrmPurchaseRequestBO.class);

            // 子项按创建时间降序排序
            srmPurchaseRequestItemsBOS.sort(Comparator.comparing(SrmPurchaseRequestItemsBO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));

            // 将该主表对应的子项列表转为 DO，并设置到 BO 中
            requestBO.setItems(BeanUtils.toBean(srmPurchaseRequestItemsBOS, SrmPurchaseRequestItemsDO.class));

            return requestBO;
        }).sorted(Comparator.comparing(SrmPurchaseRequestBO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
    }


}
