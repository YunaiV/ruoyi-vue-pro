package cn.iocoder.yudao.module.jl.dal.mysql.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;

/**
 * 客户 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface CustomerMapper extends BaseMapperX<CustomerDO> {

    default PageResult<CustomerDO> selectPage(CustomerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CustomerDO>()
                .eqIfPresent(CustomerDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(CustomerDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(CustomerDO::getName, reqVO.getName())
                .eqIfPresent(CustomerDO::getSource, reqVO.getSource())
                .eqIfPresent(CustomerDO::getPhone, reqVO.getPhone())
                .eqIfPresent(CustomerDO::getEmail, reqVO.getEmail())
                .eqIfPresent(CustomerDO::getMark, reqVO.getMark())
                .eqIfPresent(CustomerDO::getType, reqVO.getType())
                .eqIfPresent(CustomerDO::getWechat, reqVO.getWechat())
                .eqIfPresent(CustomerDO::getDoctorProfessionalRank, reqVO.getDoctorProfessionalRank())
                .eqIfPresent(CustomerDO::getHospitalDepartment, reqVO.getHospitalDepartment())
                .eqIfPresent(CustomerDO::getAcademicTitle, reqVO.getAcademicTitle())
                .eqIfPresent(CustomerDO::getAcademicCredential, reqVO.getAcademicCredential())
                .eqIfPresent(CustomerDO::getHospitalId, reqVO.getHospitalId())
                .eqIfPresent(CustomerDO::getUniversityId, reqVO.getUniversityId())
                .eqIfPresent(CustomerDO::getCompanyId, reqVO.getCompanyId())
                .orderByDesc(CustomerDO::getId));
    }

    default List<CustomerDO> selectList(CustomerExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CustomerDO>()
                .eqIfPresent(CustomerDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(CustomerDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(CustomerDO::getName, reqVO.getName())
                .eqIfPresent(CustomerDO::getSource, reqVO.getSource())
                .eqIfPresent(CustomerDO::getPhone, reqVO.getPhone())
                .eqIfPresent(CustomerDO::getEmail, reqVO.getEmail())
                .eqIfPresent(CustomerDO::getMark, reqVO.getMark())
                .eqIfPresent(CustomerDO::getWechat, reqVO.getWechat())
                .eqIfPresent(CustomerDO::getDoctorProfessionalRank, reqVO.getDoctorProfessionalRank())
                .eqIfPresent(CustomerDO::getHospitalDepartment, reqVO.getHospitalDepartment())
                .eqIfPresent(CustomerDO::getAcademicTitle, reqVO.getAcademicTitle())
                .eqIfPresent(CustomerDO::getAcademicCredential, reqVO.getAcademicCredential())
                .eqIfPresent(CustomerDO::getHospitalId, reqVO.getHospitalId())
                .eqIfPresent(CustomerDO::getUniversityId, reqVO.getUniversityId())
                .eqIfPresent(CustomerDO::getCompanyId, reqVO.getCompanyId())
                .orderByDesc(CustomerDO::getId));
    }

}
