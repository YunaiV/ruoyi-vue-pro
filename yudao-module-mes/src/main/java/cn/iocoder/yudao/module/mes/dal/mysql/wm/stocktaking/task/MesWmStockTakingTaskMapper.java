package cn.iocoder.yudao.module.mes.dal.mysql.wm.stocktaking.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.MesWmStockTakingTaskPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 盘点任务 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesWmStockTakingTaskMapper extends BaseMapperX<MesWmStockTakingTaskDO> {

    default MesWmStockTakingTaskDO selectByCode(String code) {
        return selectOne(MesWmStockTakingTaskDO::getCode, code);
    }

    default PageResult<MesWmStockTakingTaskDO> selectPage(MesWmStockTakingTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmStockTakingTaskDO>()
                .likeIfPresent(MesWmStockTakingTaskDO::getCode, reqVO.getCode())
                .likeIfPresent(MesWmStockTakingTaskDO::getName, reqVO.getName())
                .eqIfPresent(MesWmStockTakingTaskDO::getType, reqVO.getType())
                .eqIfPresent(MesWmStockTakingTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MesWmStockTakingTaskDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MesWmStockTakingTaskDO::getPlanId, reqVO.getPlanId())
                .betweenIfPresent(MesWmStockTakingTaskDO::getTakingDate, reqVO.getTakingDate())
                .orderByDesc(MesWmStockTakingTaskDO::getId));
    }

}
