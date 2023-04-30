package cn.iocoder.yudao.module.trade.service.cart;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartAddReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartListRespVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppTradeCartUpdateReqVO;
import cn.iocoder.yudao.module.trade.convert.cart.TradeCartConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartDO;
import cn.iocoder.yudao.module.trade.dal.mysql.cart.TradeCartMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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

        // 情况一：存在，则进行数量更新
        if (cart != null) {
            cartMapper.updateById(new TradeCartDO().setId(cart.getId()).setCount(count));
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
    public AppTradeCartListRespVO getCartList(Long userId) {
        // 获得购物车的商品
        List<TradeCartDO> carts = cartMapper.selectListByUserId(userId);
        carts.sort(Comparator.comparing(TradeCartDO::getId).reversed());
        // 如果未空，则返回空结果
        if (CollUtil.isEmpty(carts)) {
            return new AppTradeCartListRespVO().setValidList(emptyList())
                    .setInvalidList(emptyList());
        }

        // 查询 SPU、SKU 列表
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(convertSet(carts, TradeCartDO::getSpuId));
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(convertSet(carts, TradeCartDO::getSpuId));

        // 拼接数据
        return TradeCartConvert.INSTANCE.convertList(carts, spus, skus);
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
        if (sku == null || CommonStatusEnum.DISABLE.getStatus().equals(sku.getStatus())) {
            throw exception(SKU_NOT_EXISTS);
        }
        if (count > sku.getStock()) {
            throw exception(SKU_STOCK_NOT_ENOUGH);
        }
        return sku;
    }

}
