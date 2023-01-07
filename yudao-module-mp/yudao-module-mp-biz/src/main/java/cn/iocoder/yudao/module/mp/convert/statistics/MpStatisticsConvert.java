package cn.iocoder.yudao.module.mp.convert.statistics;

import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserSummary;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MpStatisticsConvert {

    MpStatisticsConvert INSTANCE = Mappers.getMapper(MpStatisticsConvert.class);

    List<WxDataCubeUserSummary> convertList01(List<WxDataCubeUserSummary> list);

}
