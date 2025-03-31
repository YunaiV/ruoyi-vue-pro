package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OmsShopMapper extends BaseMapperX<OmsShopDO> {

    default MPJLambdaWrapperX<OmsShopDO> bindQueryWrapper(OmsShopDO reqVO) {
        return (MPJLambdaWrapperX<OmsShopDO>) new MPJLambdaWrapperX<OmsShopDO>()
            .selectAll(OmsShopDO.class)  // 选择所有列
            .eqIfPresent(OmsShopDO::getPlatformCode, reqVO.getPlatformCode())  // 国家编码
            .betweenIfPresent(OmsShopDO::getCreateTime, new LocalDateTime[]{reqVO.getCreateTime()})  // 创建时间范围
            .betweenIfPresent(OmsShopDO::getUpdateTime, new LocalDateTime[]{reqVO.getCreateTime()})  // 更新时间范围
            .orderByAsc(OmsShopDO::getId);
    }

    default List<OmsShopDO> getByPlatformCode(String platformCode) {
        return selectList(bindQueryWrapper(OmsShopDO.builder().platformShopCode(platformCode).build()));
    }

    default OmsShopDO getByPlatformShopCode(String platformShopCode) {
        return selectOne(bindQueryWrapper(OmsShopDO.builder().platformShopCode(platformShopCode).build()));
    }


}