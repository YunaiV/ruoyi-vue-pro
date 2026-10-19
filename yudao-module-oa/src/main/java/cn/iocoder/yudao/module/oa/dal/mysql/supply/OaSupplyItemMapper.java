package cn.iocoder.yudao.module.oa.dal.mysql.supply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.supply.*;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 办公用品 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaSupplyItemMapper extends BaseMapperX<OaSupplyItemDO> {

    default PageResult<OaSupplyItemDO> selectPage(OaSupplyItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaSupplyItemDO>()
                .likeIfPresent(OaSupplyItemDO::getName, reqVO.getName())
                .likeIfPresent(OaSupplyItemDO::getNo, reqVO.getNo())
                .eqIfPresent(OaSupplyItemDO::getCategory, reqVO.getCategory())
                .eqIfPresent(OaSupplyItemDO::getManageType, reqVO.getManageType())
                .eqIfPresent(OaSupplyItemDO::getStatus, reqVO.getStatus())
                .orderByAsc(OaSupplyItemDO::getSort).orderByDesc(OaSupplyItemDO::getId));
    }

    default OaSupplyItemDO selectByNo(String no) {
        return selectOne(OaSupplyItemDO::getNo, no);
    }

    default int updateStockQuantity(Long id, Integer quantity) {
        LambdaUpdateWrapper<OaSupplyItemDO> update = new LambdaUpdateWrapper<OaSupplyItemDO>()
                .eq(OaSupplyItemDO::getId, id)
                .setIncrBy(OaSupplyItemDO::getStockQuantity, quantity);
        if (quantity < 0) {
            update.ge(OaSupplyItemDO::getStockQuantity, -quantity);
        }
        return update(update);
    }

}
