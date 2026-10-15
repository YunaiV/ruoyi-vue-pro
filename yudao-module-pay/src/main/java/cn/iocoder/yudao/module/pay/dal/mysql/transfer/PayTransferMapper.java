package cn.iocoder.yudao.module.pay.dal.mysql.transfer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PayTransferMapper extends BaseMapperX<PayTransferDO> {

    default int updateByIdAndStatus(Long id, List<Integer> whereStatuses, PayTransferDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<PayTransferDO>()
                .eq(PayTransferDO::getId, id)
                .in(PayTransferDO::getStatus, whereStatuses));
    }

    default int updateByIdAndStatus(Long id, Integer whereStatus, PayTransferDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<PayTransferDO>()
                .eq(PayTransferDO::getId, id)
                .eq(PayTransferDO::getStatus, whereStatus));
    }

    /**
     * 更新转账单，并清空上一次转账的渠道结果
     *
     * 用于转账关闭后重新发起转账，避免新的转账残留上一次的失败原因、渠道转账单号、确认收款 package 信息等
     * 特殊：本次请求没有传递的可选字段（收款人姓名、渠道额外参数、回调地址），也需要清空。原因是：MyBatis Plus 更新时会忽略为 null 的字段
     *
     * @param id 转账单编号
     * @param whereStatus 原状态
     * @param updateObj 更新对象
     * @return 更新数量
     */
    default int updateByIdAndStatusAndClearChannelResult(Long id, Integer whereStatus, PayTransferDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<PayTransferDO>()
                .eq(PayTransferDO::getId, id).eq(PayTransferDO::getStatus, whereStatus)
                .set(PayTransferDO::getChannelTransferNo, null).set(PayTransferDO::getSuccessTime, null)
                .set(PayTransferDO::getChannelErrorCode, null).set(PayTransferDO::getChannelErrorMsg, null)
                .set(PayTransferDO::getChannelNotifyData, null).set(PayTransferDO::getChannelPackageInfo, null)
                .set(updateObj.getUserName() == null, PayTransferDO::getUserName, null)
                .set(updateObj.getChannelExtras() == null, PayTransferDO::getChannelExtras, null)
                .set(updateObj.getNotifyUrl() == null, PayTransferDO::getNotifyUrl, null));
    }

    default int updateChannelPackageInfoIfAbsent(Long id, String channelPackageInfo) {
        return update(new PayTransferDO().setChannelPackageInfo(channelPackageInfo),
                new LambdaQueryWrapper<PayTransferDO>()
                        .eq(PayTransferDO::getId, id)
                        .and(wrapper -> wrapper.isNull(PayTransferDO::getChannelPackageInfo)
                                .or().eq(PayTransferDO::getChannelPackageInfo, "")));
    }

    default PayTransferDO selectByAppIdAndMerchantOrderId(Long appId, String merchantOrderId) {
        return selectOne(PayTransferDO::getAppId, appId,
                    PayTransferDO::getMerchantTransferId, merchantOrderId);
    }

    default PageResult<PayTransferDO> selectPage(PayTransferPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayTransferDO>()
                .eqIfPresent(PayTransferDO::getNo, reqVO.getNo())
                .eqIfPresent(PayTransferDO::getAppId, reqVO.getAppId())
                .eqIfPresent(PayTransferDO::getChannelCode, reqVO.getChannelCode())
                .eqIfPresent(PayTransferDO::getMerchantTransferId, reqVO.getMerchantOrderId())
                .eqIfPresent(PayTransferDO::getStatus, reqVO.getStatus())
                .likeIfPresent(PayTransferDO::getUserName, reqVO.getUserName())
                .likeIfPresent(PayTransferDO::getUserAccount, reqVO.getUserAccount())
                .eqIfPresent(PayTransferDO::getChannelTransferNo, reqVO.getChannelTransferNo())
                .betweenIfPresent(PayTransferDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayTransferDO::getId));
    }

    default List<PayTransferDO> selectListByStatus(Collection<Integer> statuses) {
        return selectList(PayTransferDO::getStatus, statuses);
    }

    default PayTransferDO selectByAppIdAndNo(Long appId, String no) {
        return selectOne(PayTransferDO::getAppId, appId,
                PayTransferDO::getNo, no);
    }

    default PayTransferDO selectByNo(String no) {
        return selectOne(PayTransferDO::getNo, no);
    }

}
