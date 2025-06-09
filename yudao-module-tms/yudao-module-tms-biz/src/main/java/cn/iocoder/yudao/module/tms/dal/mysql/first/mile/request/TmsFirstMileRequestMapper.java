package cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 头程申请单 Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsFirstMileRequestMapper extends BaseMapperX<TmsFirstMileRequestDO> {

    default PageResult<TmsFirstMileRequestDO> selectPage(TmsFirstMileRequestPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TmsFirstMileRequestDO>()
            .eqIfPresent(TmsFirstMileRequestDO::getId, reqVO.getId())
            .betweenIfPresent(TmsFirstMileRequestDO::getCreateTime, reqVO.getCreateTime())
            .likeIfPresent(TmsFirstMileRequestDO::getCode, reqVO.getCode())
            .eqIfPresent(TmsFirstMileRequestDO::getRequesterId, reqVO.getRequesterId())
            .eqIfPresent(TmsFirstMileRequestDO::getRequestDeptId, reqVO.getRequestDeptId())
            .eqIfPresent(TmsFirstMileRequestDO::getToWarehouseId, reqVO.getToWarehouseId())
            .eqIfPresent(TmsFirstMileRequestDO::getAuditStatus, reqVO.getAuditStatus())
            .eqIfPresent(TmsFirstMileRequestDO::getOrderStatus, reqVO.getOrderStatus())
            .eqIfPresent(TmsFirstMileRequestDO::getOffStatus, reqVO.getOffStatus())
            //    @Schema(description = "FBA条码")
            //    private String fbaBarCode;
            .betweenIfPresent(TmsFirstMileRequestDO::getTotalWeight, reqVO.getTotalWeight())
            .betweenIfPresent(TmsFirstMileRequestDO::getTotalVolume, reqVO.getTotalVolume())
            .orderByDesc(TmsFirstMileRequestDO::getId));
    }


    default TmsFirstMileRequestDO selectByNo(String code) {
        return selectOne(TmsFirstMileRequestDO::getCode, code);
    }
}