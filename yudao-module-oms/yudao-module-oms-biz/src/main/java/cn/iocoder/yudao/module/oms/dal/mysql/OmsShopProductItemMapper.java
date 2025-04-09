package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oms.controller.admin.product.item.vo.OmsShopProductItemPageReqVO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Oms 店铺产品项 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface OmsShopProductItemMapper extends BaseMapperX<OmsShopProductItemDO> {

    default PageResult<OmsShopProductItemDO> selectPage(OmsShopProductItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OmsShopProductItemDO>()
            .eqIfPresent(OmsShopProductItemDO::getProductId, reqVO.getProductId())
            .eqIfPresent(OmsShopProductItemDO::getShopProductId, reqVO.getShopProductId())
            .betweenIfPresent(OmsShopProductItemDO::getCreateTime, reqVO.getCreateTime())
            .orderByDesc(OmsShopProductItemDO::getId));
    }

    default List<OmsShopProductItemDO> getShopProductItemsByProductId(Long shopProductId) {
        return this.selectList(
            LambdaQueryWrapperX.create(OmsShopProductItemDO.class).eq(OmsShopProductItemDO::getShopProductId, shopProductId)
        );
    }

}