package cn.iocoder.yudao.module.promotion.dal.mysql.discount;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.discount.vo.DiscountActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountActivityDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 限时折扣活动 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DiscountActivityMapper extends BaseMapperX<DiscountActivityDO> {

    default PageResult<DiscountActivityDO> selectPage(DiscountActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DiscountActivityDO>()
                .likeIfPresent(DiscountActivityDO::getName, reqVO.getName())
                .eqIfPresent(DiscountActivityDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(DiscountActivityDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(DiscountActivityDO::getId));
    }

    /**
     * 获取指定活动编号的活动列表且
     * 开始时间和结束时间小于给定时间 dateTime 的活动列表
     *
     * @param ids      活动编号
     * @param dateTime 指定日期
     * @return 活动列表
     */
    default List<DiscountActivityDO> selectListByIdsAndDateTimeLt(Collection<Long> ids, LocalDateTime dateTime) {
        return selectList(new LambdaQueryWrapperX<DiscountActivityDO>()
                .in(DiscountActivityDO::getId, ids)
                .lt(DiscountActivityDO::getStartTime, dateTime)
                .gt(DiscountActivityDO::getEndTime, dateTime)// 开始时间 < 指定时间 < 结束时间，也就是说获取指定时间段的活动
                .orderByDesc(DiscountActivityDO::getCreateTime));
    }

}
