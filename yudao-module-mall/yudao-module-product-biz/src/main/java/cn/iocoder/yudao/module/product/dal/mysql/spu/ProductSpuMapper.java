package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuExportReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.ProductConstants;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Mapper
public interface ProductSpuMapper extends BaseMapperX<ProductSpuDO> {

    /**
     * 获取商品 SPU 分页列表数据
     *
     * @param reqVO 分页请求参数
     * @return 商品 SPU 分页列表数据
     */
    default PageResult<ProductSpuDO> selectPage(ProductSpuPageReqVO reqVO) {
        Integer tabType = reqVO.getTabType();
        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId())
                .betweenIfPresent(ProductSpuDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ProductSpuDO::getSort);
        appendTabQuery(tabType, queryWrapper);
        return selectPage(reqVO, queryWrapper);
    }

    /**
     * 查询触发警戒库存的 SPU 数量
     *
     * @return 触发警戒库存的 SPU 数量
     */
    default Long selectCount() {
        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<>();
        // 库存小于等于警戒库存
        queryWrapper.le(ProductSpuDO::getStock, ProductConstants.ALERT_STOCK)
                // 如果库存触发警戒库存且状态为回收站的话则不计入触发警戒库存的个数
                .notIn(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus());
        return selectCount(queryWrapper);
    }

    /**
     * 获得商品 SPU 分页，提供给用户 App 使用
     */
    default PageResult<ProductSpuDO> selectPage(AppProductSpuPageReqVO pageReqVO, Set<Long> categoryIds) {
        LambdaQueryWrapperX<ProductSpuDO> query = new LambdaQueryWrapperX<ProductSpuDO>()
                // 关键字匹配，目前只匹配商品名
                .likeIfPresent(ProductSpuDO::getName, pageReqVO.getKeyword())
                // 分类
                .inIfPresent(ProductSpuDO::getCategoryId, categoryIds);
        // 上架状态 且有库存
        query.eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus());
        // 推荐类型的过滤条件
        if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_HOT)) {
            query.eq(ProductSpuDO::getRecommendHot, true);
        } else if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_BENEFIT)) {
            query.eq(ProductSpuDO::getRecommendBenefit, true);
        } else if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_BEST)) {
            query.eq(ProductSpuDO::getRecommendBest, true);
        }  else if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_NEW)) {
            query.eq(ProductSpuDO::getRecommendNew, true);
        } else if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_GOOD)) {
            query.eq(ProductSpuDO::getRecommendGood, true);
        }

        // 排序逻辑
        if (Objects.equals(pageReqVO.getSortField(), AppProductSpuPageReqVO.SORT_FIELD_SALES_COUNT)) {
            query.last(String.format(" ORDER BY (sales_count + virtual_sales_count) %s, sort DESC, id DESC",
                    pageReqVO.getSortAsc() ? "ASC" : "DESC"));
        } else if (Objects.equals(pageReqVO.getSortField(), AppProductSpuPageReqVO.SORT_FIELD_PRICE)) {
            query.orderBy(true, pageReqVO.getSortAsc(), ProductSpuDO::getPrice)
                    .orderByDesc(ProductSpuDO::getSort).orderByDesc(ProductSpuDO::getId);
        } else {
            query.orderByDesc(ProductSpuDO::getSort).orderByDesc(ProductSpuDO::getId);
        }
        return selectPage(pageReqVO, query);
    }

    default List<ProductSpuDO> selectListByRecommendType(String recommendType, Integer count) {
        QueryWrapperX<ProductSpuDO> query = new QueryWrapperX<>();
        // 上架状态 且有库存
        query.eq("status", ProductSpuStatusEnum.ENABLE.getStatus()).gt("stock", 0);
        // 推荐类型的过滤条件
        if (ObjUtil.equal(recommendType, AppProductSpuPageReqVO.RECOMMEND_TYPE_HOT)) {
            query.eq("recommend_hot", true);
        } else if (ObjUtil.equal(recommendType, AppProductSpuPageReqVO.RECOMMEND_TYPE_GOOD)) {
            query.eq("recommend_good", true);
        }
        // 设置最大长度
        query.limitN(count);
        return selectList(query);
    }

    /**
     * 更新商品 SPU 库存
     *
     * @param id        商品 SPU 编号
     * @param incrCount 增加的库存数量
     */
    default void updateStock(Long id, Integer incrCount) {
        LambdaUpdateWrapper<ProductSpuDO> updateWrapper = new LambdaUpdateWrapper<ProductSpuDO>()
                // 负数，所以使用 + 号
                .setSql(" stock = stock +" + incrCount)
                .eq(ProductSpuDO::getId, id);
        update(null, updateWrapper);
    }

    /**
     * 获得 Spu 列表
     *
     * @param reqVO 查询条件
     * @return Spu 列表
     */
    default List<ProductSpuDO> selectList(ProductSpuExportReqVO reqVO) {
        Integer tabType = reqVO.getTabType();
        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eqIfPresent(ProductSpuDO::getName, reqVO.getName());
        queryWrapper.eqIfPresent(ProductSpuDO::getCategoryId, reqVO.getCategoryId());
        queryWrapper.betweenIfPresent(ProductSpuDO::getCreateTime, reqVO.getCreateTime());
        appendTabQuery(tabType, queryWrapper);
        return selectList(queryWrapper);
    }

    /**
     * 添加后台 Tab 选项的查询条件
     *
     * @param tabType      标签类型
     * @param query 查询条件
     */
    static void appendTabQuery(Integer tabType, LambdaQueryWrapperX<ProductSpuDO> query) {
        // 出售中商品
        if (ObjectUtil.equals(ProductSpuPageReqVO.FOR_SALE, tabType)) {
            query.eqIfPresent(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus());
        }
        // 仓储中商品
        if (ObjectUtil.equals(ProductSpuPageReqVO.IN_WAREHOUSE, tabType)) {
            query.eqIfPresent(ProductSpuDO::getStatus, ProductSpuStatusEnum.DISABLE.getStatus());
        }
        // 已售空商品
        if (ObjectUtil.equals(ProductSpuPageReqVO.SOLD_OUT, tabType)) {
            query.eqIfPresent(ProductSpuDO::getStock, 0);
        }
        // 警戒库存
        if (ObjectUtil.equals(ProductSpuPageReqVO.ALERT_STOCK, tabType)) {
            query.le(ProductSpuDO::getStock, ProductConstants.ALERT_STOCK)
                    // 如果库存触发警戒库存且状态为回收站的话则不在警戒库存列表展示
                    .notIn(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus());
        }
        // 回收站
        if (ObjectUtil.equals(ProductSpuPageReqVO.RECYCLE_BIN, tabType)) {
            query.eqIfPresent(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus());
        }
    }

}
