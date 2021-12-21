package cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.channel;

import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo.PayChannelPageReqVO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayChannelDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

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
                // .eqIfPresent("config", reqVO.getConfig())
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
                // .eqIfPresent("config", reqVO.getConfig())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id")        );
    }

    /**
     * 根据条件获取通道数量
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    // TODO @aquan：Mapper 的操作，和 db 保持一致
    default Integer getChannelCountByConditions(Long merchantId, Long appid, String code) {
        return this.selectCount(new QueryWrapper<PayChannelDO>().lambda()
                .eq(PayChannelDO::getMerchantId, merchantId)
                .eq(PayChannelDO::getAppId, appid)
                .eq(PayChannelDO::getCode, code)).intValue();
    }

    /**
     * 根据条件获取通道
     *
     * @param merchantId 商户编号
     * @param appid      应用编号
     * @param code       通道编码
     * @return 数量
     */
    default PayChannelDO getChannelByConditions(Long merchantId, Long appid, String code) {
        return this.selectOne((new QueryWrapper<PayChannelDO>().lambda()
                .eq(PayChannelDO::getMerchantId, merchantId)
                .eq(PayChannelDO::getAppId, appid)
                .eq(PayChannelDO::getCode, code)
        ));
    }

    /**
     * 根据支付应用ID集合获得支付渠道列表
     *
     * @param appIds 应用编号集合
     * @return 支付渠道列表
     */
    default List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds){
        return this.selectList(new QueryWrapper<PayChannelDO>().lambda()
                .in(PayChannelDO::getAppId, appIds));
    }
}
