package cn.iocoder.yudao.module.promotion.dal.mysql.point;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.point.vo.activity.PointActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointActivityDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分商城活动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface PointActivityMapper extends BaseMapperX<PointActivityDO> {

    default PageResult<PointActivityDO> selectPage(PointActivityPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointActivityDO>()
                .eqIfPresent(PointActivityDO::getStatus, reqVO.getStatus())
                .orderByDesc(PointActivityDO::getSort));
    }

    /**
     * 更新活动库存(减少)
     *
     * @param id    活动编号
     * @param count 扣减的库存数量(正数)
     * @return 影响的行数
     */
    default int updateStockDecr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<PointActivityDO>()
                .eq(PointActivityDO::getId, id)
                .ge(PointActivityDO::getStock, count)
                .setSql("stock = stock - " + count));
    }

    /**
     * 更新活动库存（增加）
     *
     * @param id    活动编号
     * @param count 增加的库存数量(正数)
     * @return 影响的行数
     */
    default int updateStockIncr(Long id, int count) {
        Assert.isTrue(count > 0);
        return update(null, new LambdaUpdateWrapper<PointActivityDO>()
                .eq(PointActivityDO::getId, id)
                .setSql("stock = stock + " + count));
    }

}