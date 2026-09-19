package cn.iocoder.yudao.module.oa.dal.mysql.vehicle;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.vehicle.vo.OaVehiclePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.vehicle.OaVehicleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 车辆 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaVehicleMapper extends BaseMapperX<OaVehicleDO> {

    default PageResult<OaVehicleDO> selectPage(OaVehiclePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaVehicleDO>()
                .likeIfPresent(OaVehicleDO::getNo, reqVO.getNo())
                .likeIfPresent(OaVehicleDO::getName, reqVO.getName())
                .eqIfPresent(OaVehicleDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(OaVehicleDO::getCategory, reqVO.getCategory())
                .eqIfPresent(OaVehicleDO::getStatus, reqVO.getStatus())
                .likeIfPresent(OaVehicleDO::getType, reqVO.getType())
                .likeIfPresent(OaVehicleDO::getBrandModel, reqVO.getBrandModel())
                .betweenIfPresent(OaVehicleDO::getCompulsoryInsuranceExpireTime, reqVO.getCompulsoryInsuranceExpireTime())
                .betweenIfPresent(OaVehicleDO::getCommercialInsuranceExpireTime, reqVO.getCommercialInsuranceExpireTime())
                .betweenIfPresent(OaVehicleDO::getInspectionExpireTime, reqVO.getInspectionExpireTime())
                .orderByDesc(OaVehicleDO::getId));
    }

    default OaVehicleDO selectByNo(String no) {
        return selectOne(OaVehicleDO::getNo, no);
    }

}
