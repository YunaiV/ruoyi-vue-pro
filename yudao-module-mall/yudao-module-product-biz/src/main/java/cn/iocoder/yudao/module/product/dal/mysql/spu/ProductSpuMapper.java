package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuExportReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.ProductConstants;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuPageTabEnum;
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
        validateTabType(tabType, queryWrapper);
        return selectPage(reqVO, queryWrapper);
    }

    /**
     * 获取库存小于 value ，且状态不等于 status 的的个数
     *
     * @return 个数
     */
    default Long selectCountByStockAndStatus() {
        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.le(ProductSpuDO::getStock, ProductConstants.ALERT_STOCK)
                // TODO @puhui999：IN 另外两个状态，会不会好点哈。尽量不用 !=
                // 如果库存触发警戒库存且状态为回收站的话则不计入触发警戒库存的个数
                .and(q -> q.ne(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus()));
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
        query.eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus()).gt(ProductSpuDO::getStock, 0);
        // 推荐类型的过滤条件
        if (ObjUtil.equal(pageReqVO.getRecommendType(), AppProductSpuPageReqVO.RECOMMEND_TYPE_HOT)) {
            query.eq(ProductSpuDO::getRecommendHot, true);
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
    default List<ProductSpuDO> selectList(ProductSpuExportReqVO reqVO){
        Integer tabType = reqVO.getTabType();
        LambdaQueryWrapperX<ProductSpuDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.eqIfPresent(ProductSpuDO::getName,reqVO.getName());
        queryWrapper.eqIfPresent(ProductSpuDO::getCategoryId,reqVO.getCategoryId());
        queryWrapper.betweenIfPresent(ProductSpuDO::getCreateTime,reqVO.getCreateTime());
        validateTabType(tabType, queryWrapper);
        return selectList(queryWrapper);
    }

    // TODO @puhui999：应该不太适合 validate 验证，应该是补充条件，例如说 appendTabQuery
    /**
     * 验证选项卡类型构建条件
     *
     * @param tabType      标签类型
     * @param queryWrapper 查询条件
     */
    static void validateTabType(Integer tabType, LambdaQueryWrapperX<ProductSpuDO> queryWrapper) {
        // 出售中商品 TODO puhui999：这样好点
        if (ObjectUtil.equals(ProductSpuPageTabEnum.FOR_SALE.getType(), tabType)) {
            queryWrapper.eqIfPresent(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus());
        }
        if (ObjectUtil.equals(ProductSpuPageTabEnum.IN_WAREHOUSE.getType(), tabType)) {
            // 仓储中商品
            queryWrapper.eqIfPresent(ProductSpuDO::getStatus, ProductSpuStatusEnum.DISABLE.getStatus());
        }
        if (ObjectUtil.equals(ProductSpuPageTabEnum.SOLD_OUT.getType(), tabType)) {
            // 已售空商品
            queryWrapper.eqIfPresent(ProductSpuDO::getStock, 0);
        }
        if (ObjectUtil.equals(ProductSpuPageTabEnum.ALERT_STOCK.getType(), tabType)) {
            queryWrapper.le(ProductSpuDO::getStock, ProductConstants.ALERT_STOCK)
                    // 如果库存触发警戒库存且状态为回收站的话则不在警戒库存列表展示
                    .and(q -> q.ne(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus()));
        }
        if (ObjectUtil.equals(ProductSpuPageTabEnum.RECYCLE_BIN.getType(), tabType)) {
            // 回收站
            queryWrapper.eqIfPresent(ProductSpuDO::getStatus, ProductSpuStatusEnum.RECYCLE.getStatus());
        }
    }
}
