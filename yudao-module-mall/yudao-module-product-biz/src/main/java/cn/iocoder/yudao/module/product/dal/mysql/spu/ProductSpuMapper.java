package cn.iocoder.yudao.module.product.dal.mysql.spu;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuTabTypeEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Objects;
import java.util.Set;

@Mapper
public interface ProductSpuMapper extends BaseMapperX<ProductSpuDO> {

    default PageResult<ProductSpuDO> selectPage(ProductSpuPageReqVO reqVO) {
        // TODO @puhui999：多个 tab，写 if else 去补条件，可阅读性会好点哈
        return selectPage(reqVO, new LambdaQueryWrapperX<ProductSpuDO>()
                // 商品名称
                .likeIfPresent(ProductSpuDO::getName, reqVO.getName())
                .betweenIfPresent(ProductSpuDO::getCreateTime, reqVO.getCreateTime())
                // 出售中商品
                .eq(ProductSpuTabTypeEnum.FOR_SALE.getType().equals(reqVO.getTabType()),ProductSpuDO::getStatus,ProductSpuStatusEnum.ENABLE.getStatus())
                // 仓储中商品
                .eq(ProductSpuTabTypeEnum.IN_WAREHOUSE.getType().equals(reqVO.getTabType()),ProductSpuDO::getStatus,ProductSpuStatusEnum.DISABLE.getStatus())
                // 已售空商品
                .eq(ProductSpuTabTypeEnum.SOLD_OUT.getType().equals(reqVO.getTabType()),ProductSpuDO::getStock,0)
                // TODO @phuui999：警戒库存暂时为 10，后期需要使用常量或者数据库配置替换
                .le(ProductSpuTabTypeEnum.ALERT_STOCK.getType().equals(reqVO.getTabType()),ProductSpuDO::getStock,10)
                // 回收站
                .eq(ProductSpuTabTypeEnum.RECYCLE_BIN.getType().equals(reqVO.getTabType()),ProductSpuDO::getStatus,ProductSpuStatusEnum.RECYCLE.getStatus())
                .orderByDesc(ProductSpuDO::getSort));
    }

    /**
     * 获得商品 SPU 分页，提供给用户 App 使用
     */
    default PageResult<ProductSpuDO> selectPage(AppProductSpuPageReqVO pageReqVO, Set<Long> categoryIds) {
        LambdaQueryWrapperX<ProductSpuDO> query = new LambdaQueryWrapperX<ProductSpuDO>()
                .likeIfPresent(ProductSpuDO::getName, pageReqVO.getKeyword()) // 关键字匹配，目前只匹配商品名
                .inIfPresent(ProductSpuDO::getCategoryId, categoryIds); // 分类
        query.eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus()) // 上架状态
                .gt(ProductSpuDO::getStock, 0); // 有库存
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
     * @param id 商品 SPU 编号
     * @param incrCount 增加的库存数量
     */
    default void updateStock(Long id, Integer incrCount) {
        LambdaUpdateWrapper<ProductSpuDO> updateWrapper = new LambdaUpdateWrapper<ProductSpuDO>()
                .setSql(" total_stock = total_stock +" + incrCount) // 负数，所以使用 + 号
                .eq(ProductSpuDO::getId, id);
        update(null, updateWrapper);
    }

}
