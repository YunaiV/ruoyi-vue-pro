package cn.iocoder.yudao.module.tms.dal.mysql.port.info;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.port.info.TmsPortInfoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * TMS港口信息 Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsPortInfoMapper extends BaseMapperX<TmsPortInfoDO> {

    default PageResult<TmsPortInfoDO> selectPage(TmsPortInfoPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TmsPortInfoDO>().eqIfPresent(TmsPortInfoDO::getCreator, reqVO.getCreator())
            .betweenIfPresent(TmsPortInfoDO::getCreateTime, reqVO.getCreateTime()).eqIfPresent(TmsPortInfoDO::getUpdater, reqVO.getUpdater())
            .betweenIfPresent(TmsPortInfoDO::getUpdateTime, reqVO.getUpdateTime()).likeIfPresent(TmsPortInfoDO::getCode, reqVO.getCode())
            .likeIfPresent(TmsPortInfoDO::getName, reqVO.getName()).likeIfPresent(TmsPortInfoDO::getNameEn, reqVO.getNameEn())
            .eqIfPresent(TmsPortInfoDO::getCountryCode, reqVO.getCountryCode()).likeIfPresent(TmsPortInfoDO::getCountryName, reqVO.getCountryName())
            .likeIfPresent(TmsPortInfoDO::getCityName, reqVO.getCityName()).likeIfPresent(TmsPortInfoDO::getCityNameEn, reqVO.getCityNameEn())
            .likeIfPresent(TmsPortInfoDO::getRemark, reqVO.getRemark()).eqIfPresent(TmsPortInfoDO::getStatus, reqVO.getStatus())
            .orderByDesc(TmsPortInfoDO::getId));
    }

}