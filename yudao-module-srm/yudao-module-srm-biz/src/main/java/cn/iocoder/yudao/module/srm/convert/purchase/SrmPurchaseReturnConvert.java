package cn.iocoder.yudao.module.srm.convert.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.refund.SrmPurchaseReturnBO;
import cn.iocoder.yudao.module.srm.service.purchase.refund.SrmPurchaseReturnItemBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface SrmPurchaseReturnConvert {

    SrmPurchaseReturnConvert INSTANCE = Mappers.getMapper(SrmPurchaseReturnConvert.class);

    /**
     * 将分页数据转换为一个主表对应多个子表的结构
     *
     * @param pageResult 原始分页数据（子表+主表）
     * @return 转换后的分页数据（主表+子表列表）
     */
    default PageResult<SrmPurchaseReturnBO> convertPage(PageResult<SrmPurchaseReturnItemBO> pageResult) {
        if (pageResult == null || pageResult.getList().isEmpty()) {
            return new PageResult<>();
        }

        // 1. 转换为目标结构
        List<SrmPurchaseReturnBO> resultList = convertToSrmPurchaseReturnBOList(pageResult.getList());

        // 2. 返回新的分页结果
        return new PageResult<>(resultList, pageResult.getTotal());
    }

    /**
     * 将列表数据转换为主表+子表结构
     *
     * @param list 原始列表数据
     * @return 主表+子表列表
     */
    default List<SrmPurchaseReturnBO> convertList(List<SrmPurchaseReturnItemBO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return convertToSrmPurchaseReturnBOList(list);
    }

    /**
     * 将 SrmPurchaseReturnItemBO 列表转换为 SrmPurchaseReturnBO 列表
     *
     * @param list SrmPurchaseReturnItemBO 列表
     * @return SrmPurchaseReturnBO 列表
     */
    default List<SrmPurchaseReturnBO> convertToSrmPurchaseReturnBOList(List<SrmPurchaseReturnItemBO> list) {
        // 1. 按主表ID分组
        Map<Long, List<SrmPurchaseReturnItemBO>> returnMap = list.stream()
            .collect(Collectors.groupingBy(item -> item.getSrmPurchaseReturnDO().getId()));

        // 2. 转换为目标结构
        return returnMap.values().stream().map(srmPurchaseReturnItemBOS -> {
                // 2.1 获取主表数据
                SrmPurchaseReturnDO returnDO = srmPurchaseReturnItemBOS.get(0).getSrmPurchaseReturnDO();
                // 2.2 转换为 BO
                SrmPurchaseReturnBO returnBO = BeanUtils.toBean(returnDO, SrmPurchaseReturnBO.class);
                // 2.3 设置子表数据
                returnBO.setSrmPurchaseReturnItemDOs(srmPurchaseReturnItemBOS.stream()
                    .map(item -> BeanUtils.toBean(item, SrmPurchaseReturnItemDO.class))
                    .sorted(Comparator.comparing(SrmPurchaseReturnItemDO::getCreateTime).reversed()) // 子表按创建时间降序排序
                    .collect(Collectors.toList()));
                return returnBO;
            }).sorted(Comparator.comparing(SrmPurchaseReturnBO::getCreateTime).reversed()) // 主表按创建时间降序排序
            .collect(Collectors.toList());
    }
} 