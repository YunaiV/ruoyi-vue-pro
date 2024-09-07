package cn.iocoder.yudao.module.iot.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * iot 产品 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductMapper extends BaseMapperX<ProductDO> {

    default PageResult<ProductDO> selectPage(ProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductDO>()
                .likeIfPresent(ProductDO::getName, reqVO.getName())
                .betweenIfPresent(ProductDO::getCreateTime, reqVO.getCreateTime())
                .likeIfPresent(ProductDO::getProductKey, reqVO.getProductKey())
                .eqIfPresent(ProductDO::getProtocolId, reqVO.getProtocolId())
                .eqIfPresent(ProductDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(ProductDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ProductDO::getValidateType, reqVO.getValidateType())
                .eqIfPresent(ProductDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProductDO::getDeviceType, reqVO.getDeviceType())
                .eqIfPresent(ProductDO::getNetType, reqVO.getNetType())
                .eqIfPresent(ProductDO::getProtocolType, reqVO.getProtocolType())
                .eqIfPresent(ProductDO::getDataFormat, reqVO.getDataFormat())
                .orderByDesc(ProductDO::getId));
    }

}