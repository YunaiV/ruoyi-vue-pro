package cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.app;

import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.PayAppExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.PayAppPageReqVO;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 支付应用信息 Mapper
 *
 * @author aquan
 */
@Mapper
public interface PayAppMapper extends BaseMapperX<PayAppDO> {

    /**
     * 分页查询
     *
     * @param reqVO       支付应用信息分页查询条件
     * @param merchantIds 商户 ID 集合
     * @return 支付应用信息
     */
    default PageResult<PayAppDO> selectPage(PayAppPageReqVO reqVO, Collection<Long> merchantIds) {
        return selectPage(reqVO, new QueryWrapperX<PayAppDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("remark", reqVO.getRemark())
                .eqIfPresent("pay_notify_url", reqVO.getPayNotifyUrl())
                .eqIfPresent("refund_notify_url", reqVO.getRefundNotifyUrl())
                .inIfPresent("merchant_id", merchantIds)
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    /**
     * 列表查询
     *
     * @param reqVO       支付应用信息 Excel 导出查询条件
     * @param merchantIds 商户 ID 集合
     * @return 支付应用信息
     */
    default List<PayAppDO> selectList(PayAppExportReqVO reqVO, Collection<Long> merchantIds) {
        return selectList(new QueryWrapperX<PayAppDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("remark", reqVO.getRemark())
                .eqIfPresent("pay_notify_url", reqVO.getPayNotifyUrl())
                .eqIfPresent("refund_notify_url", reqVO.getRefundNotifyUrl())
                .inIfPresent("merchant_id", merchantIds)
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    /**
     * 根据 商户 ID 查询支付应用信息
     * @param merchantId 商户 ID
     * @return 支付应用信息列表
     */
    default List<PayAppDO> getListByMerchantId(String merchantId){
        return selectList(new LambdaQueryWrapper<PayAppDO>()
                .select(PayAppDO::getId, PayAppDO::getName)
                .eq(PayAppDO::getMerchantId, merchantId));
    }
}
