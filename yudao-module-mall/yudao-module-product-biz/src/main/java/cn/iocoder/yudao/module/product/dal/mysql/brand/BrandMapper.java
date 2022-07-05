package cn.iocoder.yudao.module.product.dal.mysql.brand;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.BrandDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.brand.vo.*;

/**
 * 品牌 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface BrandMapper extends BaseMapperX<BrandDO> {

    default PageResult<BrandDO> selectPage(BrandPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrandDO>()
                .eqIfPresent(BrandDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(BrandDO::getName, reqVO.getName())
                .eqIfPresent(BrandDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BrandDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(BrandDO::getId));
    }

    default List<BrandDO> selectList(BrandExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<BrandDO>()
                .eqIfPresent(BrandDO::getCategoryId, reqVO.getCategoryId())
                .likeIfPresent(BrandDO::getName, reqVO.getName())
                .eqIfPresent(BrandDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BrandDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(BrandDO::getId));
    }

}
