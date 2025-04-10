package cn.iocoder.yudao.module.fms.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.payment.FmsFinancePaymentPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinancePaymentDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinancePaymentItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 付款单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsFinancePaymentMapper extends BaseMapperX<FmsFinancePaymentDO> {

    default PageResult<FmsFinancePaymentDO> selectPage(FmsFinancePaymentPageReqVO reqVO) {
        MPJLambdaWrapperX<FmsFinancePaymentDO> query = new MPJLambdaWrapperX<FmsFinancePaymentDO>()
            .likeIfPresent(FmsFinancePaymentDO::getNo, reqVO.getNo())
            .betweenIfPresent(FmsFinancePaymentDO::getPaymentTime, reqVO.getPaymentTime())
            .eqIfPresent(FmsFinancePaymentDO::getSupplierId, reqVO.getSupplierId())
            .eqIfPresent(FmsFinancePaymentDO::getCreator, reqVO.getCreator())
            .eqIfPresent(FmsFinancePaymentDO::getFinanceUserId, reqVO.getFinanceUserId())
            .eqIfPresent(FmsFinancePaymentDO::getAccountId, reqVO.getAccountId())
            .eqIfPresent(FmsFinancePaymentDO::getStatus, reqVO.getStatus())
            .likeIfPresent(FmsFinancePaymentDO::getRemark, reqVO.getRemark())
            .orderByDesc(FmsFinancePaymentDO::getId);
        if (reqVO.getBizNo() != null) {
            query.leftJoin(FmsFinancePaymentItemDO.class, FmsFinancePaymentItemDO::getPaymentId, FmsFinancePaymentDO::getId)
                .eq(reqVO.getBizNo() != null, FmsFinancePaymentItemDO::getBizNo, reqVO.getBizNo())
                .groupBy(FmsFinancePaymentDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, FmsFinancePaymentDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, FmsFinancePaymentDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<FmsFinancePaymentDO>()
            .eq(FmsFinancePaymentDO::getId, id).eq(FmsFinancePaymentDO::getStatus, status));
    }

    default FmsFinancePaymentDO selectByNo(String no) {
        return selectOne(FmsFinancePaymentDO::getNo, no);
    }

}