package cn.iocoder.yudao.module.iot.dal.mysql.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IoT 产品 Mapper
 *
 * @author ahh
 */
@Mapper
public interface IotProductMapper extends BaseMapperX<IotProductDO> {

    default PageResult<IotProductDO> selectPage(IotProductPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotProductDO>()
                .likeIfPresent(IotProductDO::getName, reqVO.getName())
                .likeIfPresent(IotProductDO::getProductKey, reqVO.getProductKey())
                .orderByDesc(IotProductDO::getId));
    }

    default IotProductDO selectByProductKey(String productKey) {
        return selectOne(new LambdaQueryWrapper<IotProductDO>()
                .apply("LOWER(product_key) = {0}", productKey.toLowerCase()));
    }

    /**
     * 统计产品数量
     *
     * @param createTime 创建时间，如果为空，则统计所有产品数量
     * @return 产品数量
     */
    default Long selectCountByCreateTime(LocalDateTime createTime) {
        return selectCount(new LambdaQueryWrapperX<IotProductDO>()
                .geIfPresent(IotProductDO::getCreateTime, createTime));
    }

    /**
     * 获得产品列表，基于分类编号
     *
     * @param categoryId 分类编号
     * @return 产品列表
     */
    default List<IotProductDO> selectListByCategoryId(Long categoryId) {
        return selectList(new LambdaQueryWrapperX<IotProductDO>()
                .eq(IotProductDO::getCategoryId, categoryId)
                .orderByDesc(IotProductDO::getId));
    }

}