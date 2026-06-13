package cn.iocoder.yudao.module.wms.dal.mysql.md.item;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.md.item.vo.sku.WmsItemSkuPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * WMS 商品 SKU Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WmsItemSkuMapper extends BaseMapperX<WmsItemSkuDO> {

    /**
     * 按 SKU 维度分页查询，支持商品 / 品牌 / 分类多表联查筛选。
     *
     * 使用 {@link MPJLambdaWrapperX} 替代 lite 的手写 XML JOIN（见 lite
     * {@code ItemSkuMapper.selectByBo}），范式参照本模块 {@code WmsInventoryMapper.selectPage}。
     */
    default PageResult<WmsItemSkuDO> selectPage(WmsItemSkuPageReqVO reqVO) {
        MPJLambdaWrapperX<WmsItemSkuDO> query = new MPJLambdaWrapperX<WmsItemSkuDO>()
                .selectAll(WmsItemSkuDO.class)
                .innerJoin(WmsItemDO.class, WmsItemDO::getId, WmsItemSkuDO::getItemId)
                .likeIfPresent(WmsItemDO::getCode, reqVO.getItemCode())
                .likeIfPresent(WmsItemDO::getName, reqVO.getItemName())
                .eqIfPresent(WmsItemDO::getCategoryId, reqVO.getCategoryId())
                .eqIfPresent(WmsItemDO::getBrandId, reqVO.getBrandId())
                .likeIfPresent(WmsItemSkuDO::getCode, reqVO.getCode())
                .likeIfPresent(WmsItemSkuDO::getName, reqVO.getName())
                .likeIfPresent(WmsItemSkuDO::getBarCode, reqVO.getBarCode())
                .orderByDesc(WmsItemSkuDO::getId);
        return selectJoinPage(reqVO, WmsItemSkuDO.class, query);
    }

    default List<WmsItemSkuDO> selectListByItemId(Long itemId) {
        return selectList(new LambdaQueryWrapperX<WmsItemSkuDO>()
                .eq(WmsItemSkuDO::getItemId, itemId)
                .orderByAsc(WmsItemSkuDO::getId));
    }

    default List<WmsItemSkuDO> selectListByItemIds(Collection<Long> itemIds) {
        if (CollUtil.isEmpty(itemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<WmsItemSkuDO>()
                .inIfPresent(WmsItemSkuDO::getItemId, itemIds)
                .orderByAsc(WmsItemSkuDO::getId));
    }

    default List<WmsItemSkuDO> selectList(Collection<Long> itemIds, String code, String name) {
        if (itemIds != null && CollUtil.isEmpty(itemIds)) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<WmsItemSkuDO>()
                .inIfPresent(WmsItemSkuDO::getItemId, itemIds)
                .likeIfPresent(WmsItemSkuDO::getCode, code)
                .likeIfPresent(WmsItemSkuDO::getName, name)
                .orderByAsc(WmsItemSkuDO::getId));
    }

    default void deleteByItemId(Long itemId) {
        delete(WmsItemSkuDO::getItemId, itemId);
    }

}
