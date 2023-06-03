package cn.iocoder.yudao.module.trade.service.cart;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartAddReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartListRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartResetReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartUpdateReqVO;
import cn.iocoder.yudao.module.trade.convert.cart.TradeCartConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartDO;
import cn.iocoder.yudao.module.trade.dal.mysql.cart.TradeCartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.CARD_ITEM_NOT_FOUND;
import static java.util.Collections.emptyList;

/**
 * 购物车 Service 实现类
 *
 * // TODO 芋艿：秒杀、拼团、砍价对购物车的影响
 * // TODO 芋艿：未来优化：购物车的价格计算，支持营销信息
 *
 * @author 芋道源码
 */
@Service
@Validated
public class TradeCartServiceImpl implements TradeCartService {

    @Resource
    private TradeCartMapper cartMapper;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public Long addCart(Long userId, AppTradeCartAddReqVO addReqVO) {
        // 查询 TradeCartDO
        TradeCartDO cart = cartMapper.selectByUserIdAndSkuId(userId, addReqVO.getSkuId(),
                addReqVO.getAddStatus(), false);
        // 校验 SKU
        Integer count = cart != null && addReqVO.getAddStatus() ?
                cart.getCount() + addReqVO.getCount() : addReqVO.getCount();
        ProductSkuRespDTO sku = checkProductSku(addReqVO.getSkuId(), count);

        // 情况零：特殊，count 小于等于 0，说明前端项目删除
        // 情况一：存在，则进行数量更新
        if (cart != null) {
            // 特殊情况，如果 count 小于等于 0，说明前端想要删除
            if (count <= 0) {
                cartMapper.deleteById(cart.getId());
            } else {
                cartMapper.updateById(new TradeCartDO().setId(cart.getId()).setCount(count));
            }
            return cart.getId();
        // 情况二：不存在，则进行插入
        } else {
            cart = new TradeCartDO().setUserId(userId)
                    .setSpuId(sku.getSpuId()).setSkuId(sku.getId()).setCount(count)
                    .setAddStatus(addReqVO.getAddStatus()).setOrderStatus(false);
            cartMapper.insert(cart);
        }
        return cart.getId();
    }

    @Override
    public void updateCart(Long userId, AppTradeCartUpdateReqVO updateReqVO) {
        // 校验 TradeCartDO 存在
        TradeCartDO cart = cartMapper.selectById(updateReqVO.getId(), userId);
        if (cart == null) {
            throw exception(CARD_ITEM_NOT_FOUND);
        }
        // 校验商品 SKU
        checkProductSku(cart.getSkuId(), updateReqVO.getCount());

        // 更新数量
        cartMapper.updateById(new TradeCartDO().setId(cart.getId())
                .setCount(updateReqVO.getCount()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetCart(Long userId, AppTradeCartResetReqVO resetReqVO) {
        // 第一步：删除原本的购物项
        TradeCartDO oldCart = cartMapper.selectById(resetReqVO.getId(), userId);
        if (oldCart == null) {
            throw exception(CARD_ITEM_NOT_FOUND);
        }
        cartMapper.deleteById(oldCart.getId());

        // 第二步：添加新的购物项
        TradeCartDO newCart = cartMapper.selectByUserIdAndSkuId(userId, resetReqVO.getSkuId(),
                true, false);
        if (newCart != null) {
            updateCart(userId, new AppTradeCartUpdateReqVO()
                    .setId(newCart.getId()).setCount(resetReqVO.getCount()));
        } else {
            addCart(userId, new AppTradeCartAddReqVO().setAddStatus(true)
                    .setSkuId(resetReqVO.getSkuId()).setCount(resetReqVO.getCount()));
        }
    }

    /**
     * 购物车删除商品
     *
     * @param userId 用户编号
     * @param ids 商品 SKU 编号的数组
     */
    @Override
    public void deleteCart(Long userId, Collection<Long> ids) {
        // 查询 TradeCartDO 列表
        List<TradeCartDO> carts = cartMapper.selectListByIds(ids, userId);
        if (CollUtil.isEmpty(carts)) {
            return;
        }

        // 批量标记删除
        cartMapper.deleteBatchIds(ids);
    }

    @Override
    public Integer getCartCount(Long userId) {
        return cartMapper.selectSumByUserId(userId);
    }

    @Override
    public Map<Long, Integer> getCartCountMap(Long userId) {
        return cartMapper.selectSumMapByUserId(userId);
    }

    @Override
    public AppTradeCartListRespVO getCartList(Long userId) {
        // 获得购物车的商品，只查询未下单的
        List<TradeCartDO> carts = cartMapper.selectListByUserId(userId, true, false);
        carts.sort(Comparator.comparing(TradeCartDO::getId).reversed());
        // 如果未空，则返回空结果
        if (CollUtil.isEmpty(carts)) {
            return new AppTradeCartListRespVO().setValidList(emptyList())
                    .setInvalidList(emptyList());
        }

        // 查询 SPU、SKU 列表
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(convertSet(carts, TradeCartDO::getSpuId));
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(convertSet(carts, TradeCartDO::getSkuId));

        // 如果 SPU 被删除，则删除购物车对应的商品。延迟删除
        deleteCartIfSpuDeleted(carts, spus);

        // 拼接数据
        return TradeCartConvert.INSTANCE.convertList(carts, spus, skus);
    }

    @Override
    public List<TradeCartDO> getCartList(Long userId, Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return cartMapper.selectListByUserId(userId, ids);
    }

    private void deleteCartIfSpuDeleted(List<TradeCartDO> carts, List<ProductSpuRespDTO> spus) {
        // 如果 SPU 被删除，则删除购物车对应的商品。延迟删除
        carts.removeIf(cart -> {
            if (spus.stream().noneMatch(spu -> spu.getId().equals(cart.getSpuId()))) {
                cartMapper.deleteById(cart.getId());
                return true;
            }
            return false;
        });
    }

    /**
     * 校验商品 SKU 是否合法
     * 1. 是否存在
     * 2. 是否下架
     * 3. 库存不足
     *
     * @param skuId 商品 SKU 编号
     * @param count 商品数量
     * @return 商品 SKU
     */
    private ProductSkuRespDTO checkProductSku(Long skuId, Integer count) {
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        if (count > sku.getStock()) {
            throw exception(SKU_STOCK_NOT_ENOUGH);
        }
        return sku;
    }

}
