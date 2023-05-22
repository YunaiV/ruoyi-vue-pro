package cn.iocoder.yudao.module.jl.dal.mysql.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CompetitorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;

/**
 * 友商 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface CompetitorMapper extends BaseMapperX<CompetitorDO> {

    default PageResult<CompetitorDO> selectPage(CompetitorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CompetitorDO>()
                .betweenIfPresent(CompetitorDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(CompetitorDO::getName, reqVO.getName())
                .likeIfPresent(CompetitorDO::getContactName, reqVO.getContactName())
                .eqIfPresent(CompetitorDO::getPhone, reqVO.getPhone())
                .eqIfPresent(CompetitorDO::getType, reqVO.getType())
                .eqIfPresent(CompetitorDO::getAdvantage, reqVO.getAdvantage())
                .eqIfPresent(CompetitorDO::getDisadvantage, reqVO.getDisadvantage())
                .eqIfPresent(CompetitorDO::getMark, reqVO.getMark())
                .orderByDesc(CompetitorDO::getId));
    }

    default List<CompetitorDO> selectList(CompetitorExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CompetitorDO>()
                .betweenIfPresent(CompetitorDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(CompetitorDO::getName, reqVO.getName())
                .likeIfPresent(CompetitorDO::getContactName, reqVO.getContactName())
                .eqIfPresent(CompetitorDO::getPhone, reqVO.getPhone())
                .eqIfPresent(CompetitorDO::getType, reqVO.getType())
                .eqIfPresent(CompetitorDO::getAdvantage, reqVO.getAdvantage())
                .eqIfPresent(CompetitorDO::getDisadvantage, reqVO.getDisadvantage())
                .eqIfPresent(CompetitorDO::getMark, reqVO.getMark())
                .orderByDesc(CompetitorDO::getId));
    }

}
