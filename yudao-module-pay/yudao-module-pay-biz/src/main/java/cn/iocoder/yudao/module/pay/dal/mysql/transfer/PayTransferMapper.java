package cn.iocoder.yudao.module.pay.dal.mysql.transfer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayTransferMapper extends BaseMapperX<PayTransferDO> {

    default int updateByIdAndStatus(Long id, List<Integer> status, PayTransferDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<PayTransferDO>()
                .eq(PayTransferDO::getId, id).in(PayTransferDO::getStatus, status));
    }

    default PayTransferDO selectByAppIdAndMerchantTransferId(Long appId, String merchantTransferId){
        return selectOne(PayTransferDO::getAppId, appId,
                    PayTransferDO::getMerchantTransferId, merchantTransferId);
    }

    default PayTransferDO selectByNo(String no){
        return selectOne(PayTransferDO::getNo, no);
    }

    default PageResult<PayTransferDO> selectPage(PayTransferPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayTransferDO>()
                .eqIfPresent(PayTransferDO::getNo, reqVO.getNo())
                .eqIfPresent(PayTransferDO::getAppId, reqVO.getAppId())
                .eqIfPresent(PayTransferDO::getChannelCode, reqVO.getChannelCode())
                .eqIfPresent(PayTransferDO::getMerchantTransferId, reqVO.getMerchantTransferId())
                .eqIfPresent(PayTransferDO::getType, reqVO.getType())
                .eqIfPresent(PayTransferDO::getStatus, reqVO.getStatus())
                .likeIfPresent(PayTransferDO::getUserName, reqVO.getUserName())
                .eqIfPresent(PayTransferDO::getChannelTransferNo, reqVO.getChannelTransferNo())
                .betweenIfPresent(PayTransferDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayTransferDO::getId));
    }

    default List<PayTransferDO> selectListByStatus(Integer status){
        return selectList(PayTransferDO::getStatus, status);
    }
}




