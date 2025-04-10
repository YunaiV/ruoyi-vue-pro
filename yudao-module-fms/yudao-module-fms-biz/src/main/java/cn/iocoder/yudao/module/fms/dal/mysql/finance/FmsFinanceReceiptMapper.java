package cn.iocoder.yudao.module.fms.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.finance.vo.receipt.FmsFinanceReceiptPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinanceReceiptDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinanceReceiptItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 收款单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsFinanceReceiptMapper extends BaseMapperX<FmsFinanceReceiptDO> {

    default PageResult<FmsFinanceReceiptDO> selectPage(FmsFinanceReceiptPageReqVO reqVO) {
        MPJLambdaWrapperX<FmsFinanceReceiptDO> query = new MPJLambdaWrapperX<FmsFinanceReceiptDO>()
            .likeIfPresent(FmsFinanceReceiptDO::getNo, reqVO.getNo())
            .betweenIfPresent(FmsFinanceReceiptDO::getReceiptTime, reqVO.getReceiptTime())
            .eqIfPresent(FmsFinanceReceiptDO::getCustomerId, reqVO.getCustomerId())
            .eqIfPresent(FmsFinanceReceiptDO::getCreator, reqVO.getCreator())
            .eqIfPresent(FmsFinanceReceiptDO::getFinanceUserId, reqVO.getFinanceUserId())
            .eqIfPresent(FmsFinanceReceiptDO::getAccountId, reqVO.getAccountId())
            .eqIfPresent(FmsFinanceReceiptDO::getStatus, reqVO.getStatus())
            .likeIfPresent(FmsFinanceReceiptDO::getRemark, reqVO.getRemark())
            .orderByDesc(FmsFinanceReceiptDO::getId);
        if (reqVO.getBizNo() != null) {
            query.leftJoin(FmsFinanceReceiptItemDO.class, FmsFinanceReceiptItemDO::getReceiptId, FmsFinanceReceiptDO::getId)
                .eq(reqVO.getBizNo() != null, FmsFinanceReceiptItemDO::getBizNo, reqVO.getBizNo())
                .groupBy(FmsFinanceReceiptDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, FmsFinanceReceiptDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, FmsFinanceReceiptDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<FmsFinanceReceiptDO>()
            .eq(FmsFinanceReceiptDO::getId, id).eq(FmsFinanceReceiptDO::getStatus, status));
    }

    default FmsFinanceReceiptDO selectByNo(String no) {
        return selectOne(FmsFinanceReceiptDO::getNo, no);
    }

}