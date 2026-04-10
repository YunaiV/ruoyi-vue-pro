package cn.iocoder.yudao.module.mes.dal.mysql.dv.checkplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MES 点检保养方案 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesDvCheckPlanMapper extends BaseMapperX<MesDvCheckPlanDO> {

    default PageResult<MesDvCheckPlanDO> selectPage(MesDvCheckPlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvCheckPlanDO>()
                .likeIfPresent(MesDvCheckPlanDO::getCode, reqVO.getCode())
                .likeIfPresent(MesDvCheckPlanDO::getName, reqVO.getName())
                .eqIfPresent(MesDvCheckPlanDO::getType, reqVO.getType())
                .eqIfPresent(MesDvCheckPlanDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MesDvCheckPlanDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MesDvCheckPlanDO::getId));
    }

    default MesDvCheckPlanDO selectByCode(String code) {
        return selectOne(MesDvCheckPlanDO::getCode, code);
    }

}
