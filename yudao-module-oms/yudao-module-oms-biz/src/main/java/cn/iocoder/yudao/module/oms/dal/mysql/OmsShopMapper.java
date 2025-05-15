package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oms.controller.admin.shop.vo.OmsShopPageReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OmsShopMapper extends BaseMapperX<OmsShopDO> {

    default MPJLambdaWrapperX<OmsShopDO> bindQueryWrapper(OmsShopPageReqVO reqVO) {
        return new MPJLambdaWrapperX<OmsShopDO>()
            .selectAll(OmsShopDO.class)  // 选择所有列
            .likeIfPresent(OmsShopDO::getName, reqVO.getName()) //店铺名称
            .eqIfPresent(OmsShopDO::getExternalId, reqVO.getExternalId()) // 外部来源唯一标识[平台店铺编码]
            .eqIfPresent(OmsShopDO::getPlatformCode, reqVO.getPlatformCode())  // 平台编码
            .eqIfPresent(OmsShopDO::getType, reqVO.getType()) // 店铺类型
            .betweenIfPresent(OmsShopDO::getCreateTime, reqVO.getCreateTime())  // 创建时间范围
            .betweenIfPresent(OmsShopDO::getUpdateTime, reqVO.getCreateTime())  // 更新时间范围
            .orderByDesc(OmsShopDO::getId);
    }

    default List<OmsShopDO> getByPlatformCode(String platformCode) {
        return selectList(bindQueryWrapper(OmsShopPageReqVO.builder().platformCode(platformCode).build()));
    }

    default OmsShopDO getByPlatformShopCode(String externalId) {
        return selectOne(bindQueryWrapper(OmsShopPageReqVO.builder().externalId(externalId).build()));
    }

    default List<OmsShopDO> getShopList(OmsShopPageReqVO reqVO) {
        return selectList(bindQueryWrapper(reqVO));
    }

    default PageResult<OmsShopDO> selectPage(OmsShopPageReqVO reqVO) {
        return selectPage(reqVO, bindQueryWrapper(reqVO));
    }
}