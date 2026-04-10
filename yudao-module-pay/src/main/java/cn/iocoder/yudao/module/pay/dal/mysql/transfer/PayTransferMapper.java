package cn.iocoder.yudao.module.pay.dal.mysql.transfer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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




