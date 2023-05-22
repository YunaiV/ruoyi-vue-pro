package cn.iocoder.yudao.module.jl.dal.mysql.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;

/**
 * CRM 模块的机构/公司 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InstitutionMapper extends BaseMapperX<InstitutionDO> {

    default PageResult<InstitutionDO> selectPage(InstitutionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InstitutionDO>()
                .betweenIfPresent(InstitutionDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(InstitutionDO::getProvince, reqVO.getProvince())
                .eqIfPresent(InstitutionDO::getCity, reqVO.getCity())
                .likeIfPresent(InstitutionDO::getName, reqVO.getName())
                .eqIfPresent(InstitutionDO::getAddress, reqVO.getAddress())
                .eqIfPresent(InstitutionDO::getMark, reqVO.getMark())
                .eqIfPresent(InstitutionDO::getType, reqVO.getType())
                .orderByDesc(InstitutionDO::getId));
    }

    default List<InstitutionDO> selectList(InstitutionExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InstitutionDO>()
                .betweenIfPresent(InstitutionDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(InstitutionDO::getProvince, reqVO.getProvince())
                .eqIfPresent(InstitutionDO::getCity, reqVO.getCity())
                .likeIfPresent(InstitutionDO::getName, reqVO.getName())
                .eqIfPresent(InstitutionDO::getAddress, reqVO.getAddress())
                .eqIfPresent(InstitutionDO::getMark, reqVO.getMark())
                .eqIfPresent(InstitutionDO::getType, reqVO.getType())
                .orderByDesc(InstitutionDO::getId));
    }

}
