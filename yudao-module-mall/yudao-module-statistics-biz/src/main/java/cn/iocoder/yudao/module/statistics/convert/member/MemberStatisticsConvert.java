package cn.iocoder.yudao.module.statistics.convert.member;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 会员统计 Convert
 *
 * @author owen
 */
@Mapper
public interface MemberStatisticsConvert {

    MemberStatisticsConvert INSTANCE = Mappers.getMapper(MemberStatisticsConvert.class);

    default List<MemberAreaStatisticsRespVO> convertList(List<Area> areaList,
                                                         Map<Integer, Integer> userCountMap,
                                                         Map<Integer, MemberAreaStatisticsRespVO> orderMap) {
        return CollectionUtils.convertList(areaList, area -> {
            MemberAreaStatisticsRespVO orderVo = Optional.ofNullable(orderMap.get(area.getId())).orElseGet(MemberAreaStatisticsRespVO::new);
            return new MemberAreaStatisticsRespVO()
                    .setAreaId(area.getId()).setAreaName(area.getName())
                    .setUserCount(MapUtil.getInt(userCountMap, area.getId(), 0))
                    .setOrderCreateCount(ObjUtil.defaultIfNull(orderVo.getOrderCreateCount(), 0))
                    .setOrderPayCount(ObjUtil.defaultIfNull(orderVo.getOrderPayCount(), 0))
                    .setOrderPayPrice(ObjUtil.defaultIfNull(orderVo.getOrderPayPrice(), 0));
        });
    }

}
