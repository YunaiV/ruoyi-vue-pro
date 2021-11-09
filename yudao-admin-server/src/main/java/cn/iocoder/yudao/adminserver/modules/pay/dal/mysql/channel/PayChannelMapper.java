package cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.channel;

import java.util.*;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.*;

/**
 * 支付渠道
 Mapper
 *
 * @author 芋艿
 */
@Mapper
public interface PayChannelMapper extends BaseMapperX<PayChannelDO> {

    default PageResult<PayChannelDO> selectPage(PayChannelPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<PayChannelDO>()
                .eqIfPresent("code", reqVO.getCode())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("remark", reqVO.getRemark())
                .eqIfPresent("fee_rate", reqVO.getFeeRate())
                .eqIfPresent("merchant_id", reqVO.getMerchantId())
                .eqIfPresent("app_id", reqVO.getAppId())
                .eqIfPresent("config", reqVO.getConfig())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

    default List<PayChannelDO> selectList(PayChannelExportReqVO reqVO) {
        return selectList(new QueryWrapperX<PayChannelDO>()
                .eqIfPresent("code", reqVO.getCode())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("remark", reqVO.getRemark())
                .eqIfPresent("fee_rate", reqVO.getFeeRate())
                .eqIfPresent("merchant_id", reqVO.getMerchantId())
                .eqIfPresent("app_id", reqVO.getAppId())
                .eqIfPresent("config", reqVO.getConfig())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

}
