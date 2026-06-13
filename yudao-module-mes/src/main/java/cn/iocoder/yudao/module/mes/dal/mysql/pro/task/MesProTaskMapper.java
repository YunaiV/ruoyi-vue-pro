package cn.iocoder.yudao.module.mes.dal.mysql.pro.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * MES 生产任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesProTaskMapper extends BaseMapperX<MesProTaskDO> {

    default PageResult<MesProTaskDO> selectPage(MesProTaskPageReqVO reqVO) {
        MPJLambdaWrapperX<MesProTaskDO> query = new MPJLambdaWrapperX<>();
        query.selectAll(MesProTaskDO.class);
        // 当需要按 checkFlag 过滤时，LEFT JOIN pro_route_process
        if (reqVO.getCheckFlag() != null) {
            query.leftJoin(MesProRouteProcessDO.class, on -> on
                    .eq(MesProRouteProcessDO::getRouteId, MesProTaskDO::getRouteId)
                    .eq(MesProRouteProcessDO::getProcessId, MesProTaskDO::getProcessId));
            query.eq(MesProRouteProcessDO::getCheckFlag, reqVO.getCheckFlag());
        }
        query.likeIfPresent(MesProTaskDO::getCode, reqVO.getCode())
                .likeIfPresent(MesProTaskDO::getName, reqVO.getName())
                .eqIfPresent(MesProTaskDO::getWorkOrderId, reqVO.getWorkOrderId())
                .eqIfPresent(MesProTaskDO::getRouteId, reqVO.getRouteId())
                .eqIfPresent(MesProTaskDO::getProcessId, reqVO.getProcessId())
                .eqIfPresent(MesProTaskDO::getWorkstationId, reqVO.getWorkstationId())
                .eqIfPresent(MesProTaskDO::getStatus, reqVO.getStatus())
                .inIfPresent(MesProTaskDO::getStatus, reqVO.getStatuses())
                .betweenIfPresent(MesProTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MesProTaskDO::getId);
        return selectPage(reqVO, query);
    }

    default List<MesProTaskDO> selectListByWorkOrderId(Long workOrderId) {
        return selectList(new LambdaQueryWrapperX<MesProTaskDO>()
                .eqIfPresent(MesProTaskDO::getWorkOrderId, workOrderId)
                .orderByDesc(MesProTaskDO::getId));
    }

    default List<MesProTaskDO> selectListByWorkOrderIds(java.util.Collection<Long> workOrderIds) {
        return selectList(new LambdaQueryWrapperX<MesProTaskDO>()
                .in(MesProTaskDO::getWorkOrderId, workOrderIds)
                .orderByDesc(MesProTaskDO::getId));
    }

    default void updateProducedQuantity(Long id,
                                        BigDecimal incrProducedQuantity,
                                        BigDecimal incrQualifyQuantity,
                                        BigDecimal incrUnqualifyQuantity) {
        update(null, new LambdaUpdateWrapper<MesProTaskDO>()
                .eq(MesProTaskDO::getId, id)
                .setSql("produced_quantity = IFNULL(produced_quantity, 0) + " + incrProducedQuantity)
                .setSql("qualify_quantity = IFNULL(qualify_quantity, 0) + " + incrQualifyQuantity)
                .setSql("unqualify_quantity = IFNULL(unqualify_quantity, 0) + " + incrUnqualifyQuantity));
    }

}
