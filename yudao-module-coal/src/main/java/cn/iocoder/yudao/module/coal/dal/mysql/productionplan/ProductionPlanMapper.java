package cn.iocoder.yudao.module.coal.dal.mysql.productionplan;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.coal.dal.dataobject.productionplan.ProductionPlanDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.coal.controller.admin.productionplan.vo.*;
import org.apache.ibatis.annotations.Param;

/**
 * 生产计划 Mapper
 *
 * @author 京京
 */
@Mapper
public interface ProductionPlanMapper extends BaseMapperX<ProductionPlanDO> {

    default List<ProductionPlanDO> selectList(ProductionPlanListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ProductionPlanDO>()
                .likeIfPresent(ProductionPlanDO::getName, reqVO.getName())
                .eqIfPresent(ProductionPlanDO::getParentId, reqVO.getParentId())
                .eqIfPresent(ProductionPlanDO::getPlanType, reqVO.getPlanType())
                .eqIfPresent(ProductionPlanDO::getPlanYear, reqVO.getPlanYear())
                .eqIfPresent(ProductionPlanDO::getPlanMonth, reqVO.getPlanMonth())
                .betweenIfPresent(ProductionPlanDO::getPlanDate, reqVO.getPlanDate())
                .eqIfPresent(ProductionPlanDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductionPlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductionPlanDO::getId));
    }

	default ProductionPlanDO selectByParentIdAndName(Long parentId, String name) {
	    return selectOne(ProductionPlanDO::getParentId, parentId, ProductionPlanDO::getName, name);
	}

    default Long selectCountByParentId(Long parentId) {
        return selectCount(ProductionPlanDO::getParentId, parentId);
    }

    default void deleteByYear(Integer year) {
        delete(new LambdaQueryWrapperX<ProductionPlanDO>()
                .eq(ProductionPlanDO::getPlanYear, year));
    }

    @Delete("DELETE FROM coal_production_plan WHERE plan_year = #{year}")
    void physicalDeleteByYear(@Param("year") Integer year);
}