package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.in.SrmPurchaseInBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.in.SrmPurchaseInItemBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.*;
import java.util.stream.Collectors;

@Mapper
public interface SrmPurchaseInConvert {

    SrmPurchaseInConvert INSTANCE = Mappers.getMapper(SrmPurchaseInConvert.class);

    /**
     * 将分页数据转换为一个主表对应多个子表的结构
     *
     * @param pageResult 原始分页数据（子表+主表）
     * @return 转换后的分页数据（主表+子表列表）
     */
    default PageResult<SrmPurchaseInBO> convertPage(PageResult<SrmPurchaseInItemBO> pageResult) {
        if (pageResult == null || pageResult.getList().isEmpty()) {
            return new PageResult<>();
        }

        // 1. 转换为目标结构
        List<SrmPurchaseInBO> resultList = convertToSrmPurchaseInBOList(pageResult.getList());

        // 2. 返回新的分页结果
        return new PageResult<>(resultList, pageResult.getTotal());
    }

    /**
     * 将列表数据转换为一个主表对应多个子表的结构
     *
     * @param list 原始列表数据（子表+主表）
     * @return 转换后的列表数据（主表+子表列表）
     */
    default List<SrmPurchaseInBO> convertList(List<SrmPurchaseInItemBO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return convertToSrmPurchaseInBOList(list);
    }

    /**
     * 采购入库项BO -> 采购入库BO
     *
     * @param itemBOList 采购入库项BO集合
     * @return 采购入库BO集合
     */
    default List<SrmPurchaseInBO> convertToSrmPurchaseInBOList(List<SrmPurchaseInItemBO> itemBOList) {
        if (CollUtil.isEmpty(itemBOList)) {
            return Collections.emptyList();
        }

        // 1. 按主表ID分组
        Map<Long, List<SrmPurchaseInItemBO>> inIdMap = itemBOList.stream()
            .collect(Collectors.groupingBy(item -> item.getSrmPurchaseInDO().getId()));

        // 2. 转换为入库BO列表
        return inIdMap.values().stream().map(inItems -> {
            if (CollUtil.isEmpty(inItems)) {
                return null;
            }
            // 子项按创建时间降序排序
            inItems.sort(Comparator.comparing(
                SrmPurchaseInItemBO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())
            ));

            SrmPurchaseInBO inBO = new SrmPurchaseInBO();
            // 获取第一个子项来设置主表信息
            SrmPurchaseInItemBO firstItem = inItems.get(0);
            if (firstItem.getSrmPurchaseInDO() == null) {
                return null;
            }
            // 设置主表基本信息
            BeanUtils.copyProperties(firstItem.getSrmPurchaseInDO(), inBO);
            // 设置子表信息
            inBO.setSrmPurchaseInItemDOS(BeanUtils.toBean(inItems, SrmPurchaseInItemDO.class));
            return inBO;
        }).filter(Objects::nonNull).sorted(Comparator.comparing(SrmPurchaseInBO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
    }
} 