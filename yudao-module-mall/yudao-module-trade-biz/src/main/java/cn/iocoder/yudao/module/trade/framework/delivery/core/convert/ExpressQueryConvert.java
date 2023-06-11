package cn.iocoder.yudao.module.trade.framework.delivery.core.convert;

import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.ExpressQueryRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kd100.Kd100ExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kd100.Kd100ExpressQueryRespDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kdniao.KdNiaoExpressQueryReqDTO;
import cn.iocoder.yudao.module.trade.framework.delivery.core.dto.provider.kdniao.KdNiaoExpressQueryRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ExpressQueryConvert {

    ExpressQueryConvert INSTANCE = Mappers.getMapper(ExpressQueryConvert.class);

    List<ExpressQueryRespDTO> convertList(List<KdNiaoExpressQueryRespDTO.ExpressTrack> expressTrackList);

    List<ExpressQueryRespDTO> convertList2(List<Kd100ExpressQueryRespDTO.ExpressTrack> expressTrackList);

    KdNiaoExpressQueryReqDTO convert(ExpressQueryReqDTO dto);

    Kd100ExpressQueryReqDTO convert2(ExpressQueryReqDTO dto);

}
