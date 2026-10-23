package cn.iocoder.yudao.module.pay.dal.mysql.refund;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PayRefundMapper extends BaseMapperX<PayRefundDO> {

    default Long selectCountByAppId(Long appId) {
        return selectCount(PayRefundDO::getAppId, appId);
    }

    default  PayRefundDO selectByAppIdAndMerchantRefundId(Long appId, String merchantRefundId) {
        return selectOne(new LambdaQueryWrapperX<PayRefundDO>()
                .eq(PayRefundDO::getAppId, appId)
                .eq(PayRefundDO::getMerchantRefundId, merchantRefundId));
    }

    default Long selectCountByAppIdAndOrderId(Long appId, Long orderId, Integer status) {
        return selectCount(new LambdaQueryWrapperX<PayRefundDO>()
                .eq(PayRefundDO::getAppId, appId)
                .eq(PayRefundDO::getOrderId, orderId)
                .eq(PayRefundDO::getStatus, status));
    }

    default  PayRefundDO selectByAppIdAndNo(Long appId, String no) {
        return selectOne(new LambdaQueryWrapperX<PayRefundDO>()
                .eq(PayRefundDO::getAppId, appId)
                .eq(PayRefundDO::getNo, no));
    }

    default PayRefundDO selectByNo(String no) {
        return selectOne(PayRefundDO::getNo, no);
    }

    /**
     * 获得退款单，不使用 MyBatis 一级缓存，用于查询退款单的最新状态
     *
     * 原因是：createRefund 不加入事务（NOT_SUPPORTED），但 Spring 事务同步仍然生效，同一次调用复用同一个 SqlSession，
     * 重复查询会命中一级缓存，查询不到其它事务（例如说，退款回调）已提交的更新
     *
     * @param id 退款单编号
     * @return 退款单
     */
    @Select("SELECT * FROM pay_refund WHERE id = #{id} AND deleted = 0")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    PayRefundDO selectLatestById(@Param("id") Long id);

    default int updateByIdAndStatus(Long id, Integer status, PayRefundDO update) {
        return update(update, new LambdaQueryWrapper<PayRefundDO>()
                .eq(PayRefundDO::getId, id).eq(PayRefundDO::getStatus, status));
    }

    /**
     * 更新退款单，并校验状态、更新时间
     *
     * 用于更新退款结果：退款单的更新时间变化，说明已被并发更新、或者已是其它退款尝试（例如说，退款失败后重新发起），则不更新
     *
     * @param id 退款单编号
     * @param status 原状态
     * @param updateTime 原更新时间
     * @param update 更新对象
     * @return 更新数量
     */
    default int updateByIdAndStatusAndUpdateTime(Long id, Integer status, LocalDateTime updateTime, PayRefundDO update) {
        return update(update, new LambdaQueryWrapper<PayRefundDO>()
                .eq(PayRefundDO::getId, id).eq(PayRefundDO::getStatus, status)
                .eq(PayRefundDO::getUpdateTime, updateTime));
    }

    /**
     * 刷新结果未知的退款单的更新时间，用于重新发起退款前的占位
     *
     * 条件：待退款、渠道未受理（没有渠道退款单号）、并且更新时间早于 maxUpdateTime（超过重新发起的间隔）。
     * 不会清空渠道结果：如果已被退款回调写入渠道退款单号、或者更新为最终状态，则更新失败
     *
     * @param id 退款单编号
     * @param maxUpdateTime 最大更新时间
     * @return 更新数量
     */
    default int updateUpdateTimeByIdAndWaiting(Long id, LocalDateTime maxUpdateTime) {
        return update(null, new LambdaUpdateWrapper<PayRefundDO>()
                .set(PayRefundDO::getUpdateTime, LocalDateTime.now())
                .eq(PayRefundDO::getId, id).eq(PayRefundDO::getStatus, PayRefundStatusEnum.WAITING.getStatus())
                .and(w -> w.isNull(PayRefundDO::getChannelRefundNo).or().eq(PayRefundDO::getChannelRefundNo, ""))
                .lt(PayRefundDO::getUpdateTime, maxUpdateTime));
    }

    /**
     * 更新退款单，并清空上一次退款的渠道结果
     *
     * 用于退款失败后重新发起退款，避免新的退款结果残留上一次的失败原因、渠道退款单号等
     *
     * @param id 退款单编号
     * @param status 原状态
     * @param update 更新对象
     * @return 更新数量
     */
    default int updateByIdAndStatusAndClearChannelResult(Long id, Integer status, PayRefundDO update) {
        return update(update, new LambdaUpdateWrapper<PayRefundDO>()
                .eq(PayRefundDO::getId, id).eq(PayRefundDO::getStatus, status)
                .set(PayRefundDO::getChannelRefundNo, null).set(PayRefundDO::getSuccessTime, null)
                .set(PayRefundDO::getChannelErrorCode, null).set(PayRefundDO::getChannelErrorMsg, null)
                .set(PayRefundDO::getChannelNotifyData, null));
    }

    default PageResult<PayRefundDO> selectPage(PayRefundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayRefundDO>()
                .eqIfPresent(PayRefundDO::getAppId, reqVO.getAppId())
                .eqIfPresent(PayRefundDO::getChannelCode, reqVO.getChannelCode())
                .likeIfPresent(PayRefundDO::getMerchantOrderId, reqVO.getMerchantOrderId())
                .likeIfPresent(PayRefundDO::getMerchantRefundId, reqVO.getMerchantRefundId())
                .likeIfPresent(PayRefundDO::getChannelOrderNo, reqVO.getChannelOrderNo())
                .likeIfPresent(PayRefundDO::getChannelRefundNo, reqVO.getChannelRefundNo())
                .eqIfPresent(PayRefundDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PayRefundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayRefundDO::getId));
    }

    default List<PayRefundDO> selectList(PayRefundExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PayRefundDO>()
                .eqIfPresent(PayRefundDO::getAppId, reqVO.getAppId())
                .eqIfPresent(PayRefundDO::getChannelCode, reqVO.getChannelCode())
                .likeIfPresent(PayRefundDO::getMerchantOrderId, reqVO.getMerchantOrderId())
                .likeIfPresent(PayRefundDO::getMerchantRefundId, reqVO.getMerchantRefundId())
                .likeIfPresent(PayRefundDO::getChannelOrderNo, reqVO.getChannelOrderNo())
                .likeIfPresent(PayRefundDO::getChannelRefundNo, reqVO.getChannelRefundNo())
                .eqIfPresent(PayRefundDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PayRefundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayRefundDO::getId));
    }

    default List<PayRefundDO> selectListByStatus(Integer status) {
        return selectList(PayRefundDO::getStatus, status);
    }
}
