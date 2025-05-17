package cn.iocoder.yudao.module.pay.convert.app;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppPageItemRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppUpdateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 支付应用信息 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface PayAppConvert {

    PayAppConvert INSTANCE = Mappers.getMapper(PayAppConvert.class);

    PayAppPageItemRespVO pageConvert (PayAppDO bean);

    PayAppDO convert(PayAppCreateReqVO bean);

    PayAppDO convert(PayAppUpdateReqVO bean);

    PayAppRespVO convert(PayAppDO bean);

    List<PayAppRespVO> convertList(List<PayAppDO> list);

    PageResult<PayAppPageItemRespVO> convertPage(PageResult<PayAppDO> page);

    default PageResult<PayAppPageItemRespVO> convertPage(PageResult<PayAppDO> pageResult, List<PayChannelDO> channels) {
        PageResult<PayAppPageItemRespVO> voPageResult = convertPage(pageResult);
        // 处理 channel 关系
        Map<Long, Set<String>> appIdChannelMap = CollectionUtils.convertMultiMap2(channels, PayChannelDO::getAppId, PayChannelDO::getCode);
        voPageResult.getList().forEach(app -> app.setChannelCodes(appIdChannelMap.get(app.getId())));
        return voPageResult;
    }

}
