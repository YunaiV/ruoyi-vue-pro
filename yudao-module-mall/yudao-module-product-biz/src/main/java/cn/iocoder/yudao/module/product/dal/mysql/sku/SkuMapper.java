package cn.iocoder.yudao.module.product.dal.mysql.sku;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.SkuDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;

/**
 * 商品sku Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SkuMapper extends BaseMapperX<SkuDO> {

    default PageResult<SkuDO> selectPage(SkuPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SkuDO>()
                .betweenIfPresent(SkuDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .eqIfPresent(SkuDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(SkuDO::getSkuStatus, reqVO.getSkuStatus())
                .eqIfPresent(SkuDO::getAttrs, reqVO.getAttrs())
                .eqIfPresent(SkuDO::getPrice, reqVO.getPrice())
                .eqIfPresent(SkuDO::getOriginalPrice, reqVO.getOriginalPrice())
                .eqIfPresent(SkuDO::getCostPrice, reqVO.getCostPrice())
                .eqIfPresent(SkuDO::getBarCode, reqVO.getBarCode())
                .eqIfPresent(SkuDO::getPicUrl, reqVO.getPicUrl())
                .orderByDesc(SkuDO::getId));
    }

    default List<SkuDO> selectList(SkuExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SkuDO>()
                .betweenIfPresent(SkuDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .eqIfPresent(SkuDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(SkuDO::getSkuStatus, reqVO.getSkuStatus())
                .eqIfPresent(SkuDO::getAttrs, reqVO.getAttrs())
                .eqIfPresent(SkuDO::getPrice, reqVO.getPrice())
                .eqIfPresent(SkuDO::getOriginalPrice, reqVO.getOriginalPrice())
                .eqIfPresent(SkuDO::getCostPrice, reqVO.getCostPrice())
                .eqIfPresent(SkuDO::getBarCode, reqVO.getBarCode())
                .eqIfPresent(SkuDO::getPicUrl, reqVO.getPicUrl())
                .orderByDesc(SkuDO::getId));
    }

}
