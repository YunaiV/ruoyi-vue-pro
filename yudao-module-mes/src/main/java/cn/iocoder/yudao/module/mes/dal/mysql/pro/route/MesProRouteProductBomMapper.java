package cn.iocoder.yudao.module.mes.dal.mysql.pro.route;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 工艺路线产品 BOM Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesProRouteProductBomMapper extends BaseMapperX<MesProRouteProductBomDO> {

    default List<MesProRouteProductBomDO> selectList(Long routeId, Long processId, Long productId) {
        return selectList(new LambdaQueryWrapperX<MesProRouteProductBomDO>()
                .eqIfPresent(MesProRouteProductBomDO::getRouteId, routeId)
                .eqIfPresent(MesProRouteProductBomDO::getProcessId, processId)
                .eqIfPresent(MesProRouteProductBomDO::getProductId, productId));
    }

    default MesProRouteProductBomDO selectByUnique(Long itemId, Long processId, Long productId) {
        return selectOne(new LambdaQueryWrapperX<MesProRouteProductBomDO>()
                .eq(MesProRouteProductBomDO::getItemId, itemId)
                .eq(MesProRouteProductBomDO::getProcessId, processId)
                .eq(MesProRouteProductBomDO::getProductId, productId));
    }

    default void deleteByRouteId(Long routeId) {
        delete(new LambdaQueryWrapperX<MesProRouteProductBomDO>()
                .eq(MesProRouteProductBomDO::getRouteId, routeId));
    }

    default void deleteByRouteIdAndProductId(Long routeId, Long productId) {
        delete(new LambdaQueryWrapperX<MesProRouteProductBomDO>()
                .eq(MesProRouteProductBomDO::getRouteId, routeId)
                .eq(MesProRouteProductBomDO::getProductId, productId));
    }

    default List<MesProRouteProductBomDO> selectListByRouteIdAndProductId(Long routeId, Long productId) {
        return selectList(new LambdaQueryWrapperX<MesProRouteProductBomDO>()
                .eq(MesProRouteProductBomDO::getRouteId, routeId)
                .eq(MesProRouteProductBomDO::getProductId, productId));
    }

}
