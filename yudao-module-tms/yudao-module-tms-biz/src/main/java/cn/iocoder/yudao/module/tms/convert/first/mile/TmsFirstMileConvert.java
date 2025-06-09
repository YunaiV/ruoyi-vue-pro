package cn.iocoder.yudao.module.tms.convert.first.mile;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileExcelVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileBO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileItemBO;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.FIRST_MILE_ITEM_LIST_NOT_EMPTY;

/**
 * 头程单 Convert
 *
 * @author wdy
 */
@Slf4j
public class TmsFirstMileConvert {

    /**
     * 将明细BO列表按主表ID分组，并转换为BO对象列表
     *
     * @param itemBOList 明细BO列表
     * @return BO对象列表
     */
    public static List<TmsFirstMileBO> convertBOList(List<TmsFirstMileItemBO> itemBOList) {
        // 按主表ID分组
        Map<Long, List<TmsFirstMileItemBO>> itemMap = itemBOList.stream().collect(Collectors.groupingBy(item -> item.getTmsFirstMileDO().getId()));
        //tracking Map,默认上游类型都一样，不一样的话，需要处理
        Map<Long, TmsVesselTrackingDO> trackingMap = itemBOList.stream()
            .map(TmsFirstMileItemBO::getTmsVesselTrackingDO)
            .filter(Objects::nonNull) // 保证后续不会NPE
            .collect(Collectors.toMap(
                TmsVesselTrackingDO::getUpstreamId,
                Function.identity()
            ));

        log.debug("转换主表BO列表，itemMap: {}", itemMap);
        // 转换为主表BO列表
        return itemMap.values().stream()
            .map(tmsFirstMileItemBOS -> {
                // 获取第一个明细项的主表信息（同一主表ID下的所有明细项，其主表信息相同）
                TmsFirstMileItemBO firstItem = tmsFirstMileItemBOS.stream().findFirst().orElseThrow(() -> exception(FIRST_MILE_ITEM_LIST_NOT_EMPTY));
                //降序
                tmsFirstMileItemBOS.sort(Comparator.comparing(TmsFirstMileItemBO::getCreateTime).reversed());
                return BeanUtils.toBean(firstItem.getTmsFirstMileDO(), TmsFirstMileBO.class, bo -> {
                    bo.setItems(BeanUtils.toBean(tmsFirstMileItemBOS, TmsFirstMileItemDO.class)); //头程单明细
                    bo.setTracking(trackingMap.get(firstItem.getFirstMileId())); //最新跟踪信息
                    //分页暂时不返回 费用明细
                });
            })
            .sorted(Comparator.comparing(TmsFirstMileBO::getCreateTime).reversed())
            .toList()
            ;
    }

    /**
     * BO 列表转 Excel VO 列表
     *
     * @param list BO 列表
     * @return Excel VO 列表
     */
    public static List<TmsFirstMileExcelVO> convertExcelList(List<TmsFirstMileBO> list) {
        return list.stream().map(bo -> {
            TmsFirstMileExcelVO vo = BeanUtils.toBean(bo, TmsFirstMileExcelVO.class);
            // 转换子表信息
//            if (bo.getItems() != null) {
//                vo.setItems(BeanUtils.toBean(bo.getItems(), TmsFirstMileItemExcelVO.class));
//            }
            // 设置最新跟踪信息
            if (bo.getTracking() != null) {
                vo.setLatestTrackTime(bo.getTracking().getLastSyncTime());
                vo.setLatestTrackStatus(bo.getTracking().getTrackingStatus());
            }
            return vo;
        }).toList();
    }
} 