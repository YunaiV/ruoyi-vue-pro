package cn.iocoder.yudao.module.iot.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.category.IotProductCategoryPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 产品分类 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotProductCategoryMapper extends BaseMapperX<IotProductCategoryDO> {

    default PageResult<IotProductCategoryDO> selectPage(IotProductCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotProductCategoryDO>()
                .likeIfPresent(IotProductCategoryDO::getName, reqVO.getName())
                .betweenIfPresent(IotProductCategoryDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(IotProductCategoryDO::getSort));
    }

    default List<IotProductCategoryDO> selectListByStatus(Integer status) {
        return selectList(IotProductCategoryDO::getStatus, status);
    }

    /**
     * 统计产品分类数量
     *
     * @param createTime 创建时间，如果为空，则统计所有分类数量
     * @return 产品分类数量
     */
    default Long selectCountByCreateTime(LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotProductCategoryDO>()
                .geIfPresent(IotProductCategoryDO::getCreateTime, createTime));
    }

}