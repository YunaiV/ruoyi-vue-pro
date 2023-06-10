package cn.iocoder.yudao.module.point.dal.mysql.pointrecord;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.point.dal.dataobject.pointrecord.PointRecordDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo.*;

/**
 * 用户积分记录 Mapper
 *
 * @author QingX
 */
@Mapper
public interface PointRecordMapper extends BaseMapperX<PointRecordDO> {

    default PageResult<PointRecordDO> selectPage(PointRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointRecordDO>()
                .eqIfPresent(PointRecordDO::getBizId, reqVO.getBizId())
                .eqIfPresent(PointRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(PointRecordDO::getType, reqVO.getType())
                .eqIfPresent(PointRecordDO::getTitle, reqVO.getTitle())
                .eqIfPresent(PointRecordDO::getStatus, reqVO.getStatus())
                .orderByDesc(PointRecordDO::getId));
    }

    default List<PointRecordDO> selectList(PointRecordExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PointRecordDO>()
                .eqIfPresent(PointRecordDO::getBizId, reqVO.getBizId())
                .eqIfPresent(PointRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(PointRecordDO::getType, reqVO.getType())
                .eqIfPresent(PointRecordDO::getTitle, reqVO.getTitle())
                .eqIfPresent(PointRecordDO::getStatus, reqVO.getStatus())
                .orderByDesc(PointRecordDO::getId));
    }

}
