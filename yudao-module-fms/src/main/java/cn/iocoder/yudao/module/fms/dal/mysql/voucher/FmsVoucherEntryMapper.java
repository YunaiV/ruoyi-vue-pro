package cn.iocoder.yudao.module.fms.dal.mysql.voucher;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherSubjectAmountVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherEntryDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * FMS 凭证分录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsVoucherEntryMapper extends BaseMapperX<FmsVoucherEntryDO> {

    default List<FmsVoucherEntryDO> selectListByVoucherId(Long voucherId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getVoucherId, voucherId)
                .orderByAsc(FmsVoucherEntryDO::getSort)
                .orderByAsc(FmsVoucherEntryDO::getId));
    }

    default List<FmsVoucherEntryDO> selectListByVoucherIds(Collection<Long> voucherIds) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .in(FmsVoucherEntryDO::getVoucherId, voucherIds)
                .orderByAsc(FmsVoucherEntryDO::getVoucherId)
                .orderByAsc(FmsVoucherEntryDO::getSort)
                .orderByAsc(FmsVoucherEntryDO::getId));
    }

    default List<FmsVoucherEntryDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsVoucherEntryDO::getId));
    }

    default List<FmsVoucherEntryDO> selectListByAccountSetIdAndSubjectId(Long accountSetId, Long subjectId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                .eq(FmsVoucherEntryDO::getSubjectId, subjectId)
                .orderByAsc(FmsVoucherEntryDO::getId));
    }

    default List<FmsVoucherEntryDO> selectVoucherIdsByCondition(Long accountSetId, Collection<Long> subjectIds, String digest,
                                                                BigDecimal minAmount, BigDecimal maxAmount) {
        LambdaQueryWrapperX<FmsVoucherEntryDO> query = new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                .inIfPresent(FmsVoucherEntryDO::getSubjectId, subjectIds)
                .likeIfPresent(FmsVoucherEntryDO::getDigest, digest);
        query.apply(minAmount != null, "(debit_amount + credit_amount) >= {0}", minAmount)
                .apply(maxAmount != null, "(debit_amount + credit_amount) <= {0}", maxAmount);
        query.select(FmsVoucherEntryDO::getVoucherId);
        return selectList(query);
    }

    default void deleteByVoucherIds(Collection<Long> voucherIds) {
        delete(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .in(FmsVoucherEntryDO::getVoucherId, voucherIds));
    }

    default Long selectCountByAccountSetIdAndSubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        return selectCount(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                .in(FmsVoucherEntryDO::getSubjectId, subjectIds));
    }

    default Long selectQuantityCountByAccountSetIdAndSubjectIds(Long accountSetId, Collection<Long> subjectIds) {
        return selectCount(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                .in(FmsVoucherEntryDO::getSubjectId, subjectIds)
                .isNotNull(FmsVoucherEntryDO::getQuantity)
                .ne(FmsVoucherEntryDO::getQuantity, BigDecimal.ZERO));
    }

    default void updateSubject(Long accountSetId, Long subjectId, FmsVoucherEntryDO updateObj) {
        update(updateObj, new LambdaUpdateWrapper<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                .eq(FmsVoucherEntryDO::getSubjectId, subjectId));
    }

    default List<FmsVoucherSubjectAmountVO> selectSubjectAmountList(Long accountSetId,
                                                                    LocalDateTime beginTime, LocalDateTime endTime,
                                                                    Long voucherWordId, Integer minVoucherNumber, Integer maxVoucherNumber) {
        return selectJoinList(FmsVoucherSubjectAmountVO.class,
                new MPJLambdaWrapperX<FmsVoucherEntryDO>()
                        .selectAs(FmsVoucherEntryDO::getSubjectId,
                                FmsVoucherSubjectAmountVO::getSubjectId)
                        .selectSum(FmsVoucherEntryDO::getDebitAmount,
                                FmsVoucherSubjectAmountVO::getDebitAmount)
                        .selectSum(FmsVoucherEntryDO::getCreditAmount,
                                FmsVoucherSubjectAmountVO::getCreditAmount)
                        .eq(FmsVoucherEntryDO::getAccountSetId, accountSetId)
                        .eqIfPresent(FmsVoucherDO::getVoucherWordId, voucherWordId)
                        .geIfPresent(FmsVoucherDO::getVoucherNumber, minVoucherNumber)
                        .leIfPresent(FmsVoucherDO::getVoucherNumber, maxVoucherNumber)
                        .ge(FmsVoucherDO::getVoucherTime, beginTime)
                        .lt(FmsVoucherDO::getVoucherTime, endTime)
                        .innerJoin(FmsVoucherDO.class, FmsVoucherDO::getId,
                                FmsVoucherEntryDO::getVoucherId)
                        .groupBy(FmsVoucherEntryDO::getSubjectId)
                        .orderByAsc(FmsVoucherEntryDO::getSubjectId));
    }

    default List<FmsVoucherSubjectAmountVO> selectSubjectAmountListByVoucherIds(Collection<Long> voucherIds) {
        return selectJoinList(FmsVoucherSubjectAmountVO.class,
                new MPJLambdaWrapperX<FmsVoucherEntryDO>()
                        .selectAs(FmsVoucherEntryDO::getSubjectId,
                                FmsVoucherSubjectAmountVO::getSubjectId)
                        .selectSum(FmsVoucherEntryDO::getDebitAmount,
                                FmsVoucherSubjectAmountVO::getDebitAmount)
                        .selectSum(FmsVoucherEntryDO::getCreditAmount,
                                FmsVoucherSubjectAmountVO::getCreditAmount)
                        .in(FmsVoucherEntryDO::getVoucherId, voucherIds)
                        .groupBy(FmsVoucherEntryDO::getSubjectId)
                        .orderByAsc(FmsVoucherEntryDO::getSubjectId));
    }

}
