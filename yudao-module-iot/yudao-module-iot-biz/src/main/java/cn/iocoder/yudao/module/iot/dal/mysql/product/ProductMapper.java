package cn.iocoder.yudao.module.iot.dal.mysql.product;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import jakarta.validation.constraints.NotEmpty;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.*;

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
                .eqIfPresent(ProductDO::getIdentification, reqVO.getIdentification())
                .eqIfPresent(ProductDO::getDeviceType, reqVO.getDeviceType())
                .likeIfPresent(ProductDO::getManufacturerName, reqVO.getManufacturerName())
                .eqIfPresent(ProductDO::getModel, reqVO.getModel())
                .eqIfPresent(ProductDO::getDataFormat, reqVO.getDataFormat())
                .eqIfPresent(ProductDO::getProtocolType, reqVO.getProtocolType())
                .eqIfPresent(ProductDO::getDescription, reqVO.getDescription())
                .eqIfPresent(ProductDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ProductDO::getMetadata, reqVO.getMetadata())
                .eqIfPresent(ProductDO::getMessageProtocol, reqVO.getMessageProtocol())
                .likeIfPresent(ProductDO::getProtocolName, reqVO.getProtocolName())
                .betweenIfPresent(ProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductDO::getId));
    }

    default ProductDO selectByIdentification(String identification){
        return selectOne(new LambdaQueryWrapperX<ProductDO>().eq(ProductDO::getIdentification, identification));
    }
}