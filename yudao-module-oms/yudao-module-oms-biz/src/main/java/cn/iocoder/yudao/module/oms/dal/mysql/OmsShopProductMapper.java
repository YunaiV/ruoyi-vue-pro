package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oms.controller.admin.product.vo.OmsShopProductPageReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;


/**
 * OMS店铺产品 Mapper
 *
 * @author gumaomao
 */
@Mapper
public interface OmsShopProductMapper extends BaseMapperX<OmsShopProductDO> {


    default PageResult<OmsShopProductDO> selectPage(OmsShopProductPageReqVO reqVO) {

        // 构建查询，应用条件并进行分页查询
        MPJLambdaWrapper<OmsShopProductDO> query = new MPJLambdaWrapperX<OmsShopProductDO>()
            .likeIfPresent(OmsShopProductDO::getName, reqVO.getName())
            .likeIfPresent(OmsShopProductDO::getPlatformCode, reqVO.getPlatformProductCode())
            .betweenIfPresent(OmsShopProductDO::getCreateTime, reqVO.getCreateTime())
            .eqIfPresent(OmsShopProductDO::getUrl, reqVO.getUrl())
            .eqIfPresent(OmsShopProductDO::getShopId, reqVO.getShopId())
            .eqIfPresent(OmsShopProductDO::getDeptId, reqVO.getDeptId())
            .orderByDesc(OmsShopProductDO::getId)
            .innerJoin(OmsShopDO.class, OmsShopDO::getId, OmsShopProductDO::getShopId)  // 连接店铺表
            .likeIfExists(OmsShopDO::getPlatformCode, reqVO.getPlatformCode());

        return selectJoinPage(reqVO, OmsShopProductDO.class, query);
    }

}