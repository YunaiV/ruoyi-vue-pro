package cn.iocoder.yudao.module.mes.dal.mysql.cal.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 排班计划 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesCalPlanMapper extends BaseMapperX<MesCalPlanDO> {

    default PageResult<MesCalPlanDO> selectPage(MesCalPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesCalPlanDO>()
                .likeIfPresent(MesCalPlanDO::getCode, reqVO.getCode())
                .likeIfPresent(MesCalPlanDO::getName, reqVO.getName())
                .eqIfPresent(MesCalPlanDO::getShiftType, reqVO.getShiftType())
                .eqIfPresent(MesCalPlanDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MesCalPlanDO::getCalendarType, reqVO.getCalendarType())
                .betweenIfPresent(MesCalPlanDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(MesCalPlanDO::getEndDate, reqVO.getEndDate())
                .orderByDesc(MesCalPlanDO::getId));
    }

    default MesCalPlanDO selectByCode(String code) {
        return selectOne(MesCalPlanDO::getCode, code);
    }

}
