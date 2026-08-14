package cn.iocoder.yudao.module.fms.dal.mysql.voucher;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.voucher.FmsVoucherDO;
import cn.iocoder.yudao.module.fms.enums.voucher.FmsVoucherTidyTypeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * FMS 凭证 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsVoucherMapper extends BaseMapperX<FmsVoucherDO> {

    default void updateReviewStatusById(Long id, Integer status, Long reviewerUserId) {
        update(new LambdaUpdateWrapper<FmsVoucherDO>()
                .eq(FmsVoucherDO::getId, id)
                .set(FmsVoucherDO::getStatus, status)
                .set(FmsVoucherDO::getReviewerUserId, reviewerUserId));
    }

    default PageResult<FmsVoucherDO> selectPage(FmsVoucherPageReqVO reqVO, Collection<Long> voucherIds) {
        LambdaQueryWrapperX<FmsVoucherDO> query = new LambdaQueryWrapperX<FmsVoucherDO>()
                .eq(FmsVoucherDO::getAccountSetId, reqVO.getAccountSetId())
                .inIfPresent(FmsVoucherDO::getId, reqVO.getIds())
                .betweenIfPresent(FmsVoucherDO::getVoucherTime, reqVO.getVoucherTime())
                .eqIfPresent(FmsVoucherDO::getVoucherWordId, reqVO.getVoucherWordId())
                .eqIfPresent(FmsVoucherDO::getVoucherNumber, reqVO.getVoucherNumber())
                .eqIfPresent(FmsVoucherDO::getStatus, reqVO.getStatus())
                .eqIfPresent(FmsVoucherDO::getCreator, reqVO.getCreatorUserId() == null ? null : reqVO.getCreatorUserId().toString())
                .inIfPresent(FmsVoucherDO::getId, voucherIds);
        return selectPage(reqVO, query.orderByAsc(FmsVoucherDO::getVoucherTime)
                .orderByAsc(FmsVoucherDO::getVoucherWordId)
                .orderByAsc(FmsVoucherDO::getVoucherNumber)
                .orderByAsc(FmsVoucherDO::getId));
    }

    default FmsVoucherDO selectByAccountSetIdAndVoucherWordIdAndVoucherNumberAndVoucherTimeBetween(
            Long accountSetId, Long voucherWordId, Integer voucherNumber,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return selectOne(new LambdaQueryWrapperX<FmsVoucherDO>()
                .eq(FmsVoucherDO::getAccountSetId, accountSetId)
                .eq(FmsVoucherDO::getVoucherWordId, voucherWordId)
                .eq(FmsVoucherDO::getVoucherNumber, voucherNumber)
                .ge(FmsVoucherDO::getVoucherTime, beginTime)
                .lt(FmsVoucherDO::getVoucherTime, endTime));
    }

    default FmsVoucherDO selectLastByPeriod(Long accountSetId, Long voucherWordId, LocalDateTime monthBeginTime,
            LocalDateTime nextMonthBeginTime) {
        return selectLastOne(new LambdaQueryWrapperX<FmsVoucherDO>()
                .eq(FmsVoucherDO::getAccountSetId, accountSetId)
                .eq(FmsVoucherDO::getVoucherWordId, voucherWordId)
                .ge(FmsVoucherDO::getVoucherTime, monthBeginTime)
                .lt(FmsVoucherDO::getVoucherTime, nextMonthBeginTime)
                .orderByAsc(FmsVoucherDO::getVoucherNumber)
                .orderByAsc(FmsVoucherDO::getId));
    }

    default List<FmsVoucherDO> selectListByIdsAndAccountSetId(Collection<Long> ids, Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherDO>()
                .in(FmsVoucherDO::getId, ids)
                .eq(FmsVoucherDO::getAccountSetId, accountSetId));
    }

    default List<FmsVoucherDO> selectListByAccountSetIdAndVoucherTimeBetween(Long accountSetId,
            LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherDO>()
                .eq(FmsVoucherDO::getAccountSetId, accountSetId)
                .ge(FmsVoucherDO::getVoucherTime, beginTime)
                .lt(FmsVoucherDO::getVoucherTime, endTime)
                .orderByAsc(FmsVoucherDO::getVoucherWordId)
                .orderByAsc(FmsVoucherDO::getVoucherNumber)
                .orderByAsc(FmsVoucherDO::getId));
    }

    /**
     * 查询指定会计期间和起始号码后的待整理凭证：按凭证字分组，并根据整理方式按日期或凭证号优先排序
     */
    default List<FmsVoucherDO> selectListForTidy(Long accountSetId, LocalDateTime monthBeginTime,
            LocalDateTime nextMonthBeginTime, Long voucherWordId, Integer startNumber, Integer type) {
        LambdaQueryWrapperX<FmsVoucherDO> query = new LambdaQueryWrapperX<>();
        query.eq(FmsVoucherDO::getAccountSetId, accountSetId);
        query.eqIfPresent(FmsVoucherDO::getVoucherWordId, voucherWordId);
        query.ge(FmsVoucherDO::getVoucherTime, monthBeginTime)
                .lt(FmsVoucherDO::getVoucherTime, nextMonthBeginTime)
                .ge(FmsVoucherDO::getVoucherNumber, startNumber)
                .orderByAsc(FmsVoucherDO::getVoucherWordId);
        if (FmsVoucherTidyTypeEnum.REORDER_BY_TIME.getType().equals(type)) {
            query.orderByAsc(FmsVoucherDO::getVoucherTime)
                    .orderByAsc(FmsVoucherDO::getVoucherNumber);
        } else {
            query.orderByAsc(FmsVoucherDO::getVoucherNumber)
                    .orderByAsc(FmsVoucherDO::getVoucherTime);
        }
        return selectList(query.orderByAsc(FmsVoucherDO::getId));
    }

    /**
     * 查询指定会计期间内目标号码到原凭证号码之间的凭证：按凭证号、日期和编号升序，供移动时稳定顺延中间凭证
     */
    default List<FmsVoucherDO> selectListForMove(Long accountSetId, Long voucherWordId,
            LocalDateTime monthBeginTime, LocalDateTime nextMonthBeginTime,
            Integer targetNumber, Integer sourceNumber) {
        LambdaQueryWrapperX<FmsVoucherDO> query = new LambdaQueryWrapperX<>();
        query.eq(FmsVoucherDO::getAccountSetId, accountSetId);
        query.eq(FmsVoucherDO::getVoucherWordId, voucherWordId);
        query.ge(FmsVoucherDO::getVoucherTime, monthBeginTime)
                .lt(FmsVoucherDO::getVoucherTime, nextMonthBeginTime)
                .between(FmsVoucherDO::getVoucherNumber, targetNumber, sourceNumber)
                .orderByAsc(FmsVoucherDO::getVoucherNumber)
                .orderByAsc(FmsVoucherDO::getVoucherTime)
                .orderByAsc(FmsVoucherDO::getId);
        return selectList(query);
    }

    default Long selectCountByAccountSetIdAndVoucherWordId(Long accountSetId, Long voucherWordId) {
        return selectCount(new LambdaQueryWrapperX<FmsVoucherDO>()
                .eq(FmsVoucherDO::getAccountSetId, accountSetId)
                .eq(FmsVoucherDO::getVoucherWordId, voucherWordId));
    }

}
