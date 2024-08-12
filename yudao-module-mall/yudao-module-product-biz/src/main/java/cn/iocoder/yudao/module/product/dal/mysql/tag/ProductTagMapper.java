package cn.iocoder.yudao.module.product.dal.mysql.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.tag.ProductTagDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductTagMapper extends BaseMapperX<ProductTagDO> {

    default PageResult<ProductTagDO> selectPage(ProductTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductTagDO>()
                .likeIfPresent(ProductTagDO::getName, reqVO.getName())
                .eqIfPresent(ProductTagDO::getStatus, reqVO.getStatus())
                .orderByDesc(ProductTagDO::getId));
    }

    default ProductTagDO selectByName(String name) {
        return selectOne(ProductTagDO::getName, name);
    }
}
