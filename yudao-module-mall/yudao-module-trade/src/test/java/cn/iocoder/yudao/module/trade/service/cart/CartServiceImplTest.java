package cn.iocoder.yudao.module.trade.service.cart;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppCartAddReqVO;
import cn.iocoder.yudao.module.trade.controller.app.cart.vo.AppCartUpdateCountReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import cn.iocoder.yudao.module.trade.dal.mysql.cart.CartMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link CartServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class CartServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartMapper cartMapper;
    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private ProductSkuApi productSkuApi;

    @Test
    public void testAddCart_insert() {
        // 准备参数
        Long userId = 1L;
        AppCartAddReqVO reqVO = new AppCartAddReqVO().setSkuId(10L).setCount(3);
        // mock 方法
        when(productSkuApi.getSku(eq(10L))).thenReturn(new ProductSkuRespDTO().setId(10L).setSpuId(20L).setStock(10));
        when(cartMapper.insert(any(CartDO.class))).thenAnswer(invocation -> {
            invocation.<CartDO>getArgument(0).setId(100L);
            return 1;
        });

        // 调用
        Long cartId = cartService.addCart(userId, reqVO);
        // 断言
        assertEquals(100L, cartId);
        ArgumentCaptor<CartDO> cartCaptor = ArgumentCaptor.forClass(CartDO.class);
        verify(cartMapper).insert(cartCaptor.capture());
        CartDO cart = cartCaptor.getValue();
        assertEquals(userId, cart.getUserId());
        assertEquals(20L, cart.getSpuId());
        assertEquals(10L, cart.getSkuId());
        assertEquals(3, cart.getCount());
        assertTrue(cart.getSelected());
        verify(cartMapper, never()).updateById(any(CartDO.class));
    }

    @Test
    public void testAddCart_mergeBelowStock() {
        // 准备参数
        Long userId = 1L;
        AppCartAddReqVO reqVO = new AppCartAddReqVO().setSkuId(10L).setCount(2);
        // mock 数据
        CartDO dbCart = new CartDO().setId(100L).setUserId(userId).setSpuId(20L).setSkuId(10L)
                .setCount(3).setSelected(false);
        when(cartMapper.selectByUserIdAndSkuId(eq(userId), eq(10L))).thenReturn(dbCart);
        // mock 方法
        when(productSkuApi.getSku(eq(10L))).thenReturn(new ProductSkuRespDTO().setId(10L).setSpuId(20L).setStock(10));

        // 调用
        Long cartId = cartService.addCart(userId, reqVO);
        // 断言
        assertEquals(100L, cartId);
        ArgumentCaptor<CartDO> cartCaptor = ArgumentCaptor.forClass(CartDO.class);
        verify(cartMapper).updateById(cartCaptor.capture());
        CartDO cart = cartCaptor.getValue();
        assertEquals(100L, cart.getId());
        assertEquals(5, cart.getCount());
        assertTrue(cart.getSelected());
        verify(cartMapper, never()).insert(any(CartDO.class));
    }

    @Test
    public void testAddCart_mergeEqualsStock() {
        // 准备参数
        Long userId = 1L;
        AppCartAddReqVO reqVO = new AppCartAddReqVO().setSkuId(10L).setCount(4);
        // mock 数据
        CartDO dbCart = new CartDO().setId(100L).setUserId(userId).setSpuId(20L).setSkuId(10L)
                .setCount(6).setSelected(true);
        when(cartMapper.selectByUserIdAndSkuId(eq(userId), eq(10L))).thenReturn(dbCart);
        // mock 方法
        when(productSkuApi.getSku(eq(10L))).thenReturn(new ProductSkuRespDTO().setId(10L).setSpuId(20L).setStock(10));

        // 调用
        Long cartId = cartService.addCart(userId, reqVO);
        // 断言
        assertEquals(100L, cartId);
        ArgumentCaptor<CartDO> cartCaptor = ArgumentCaptor.forClass(CartDO.class);
        verify(cartMapper).updateById(cartCaptor.capture());
        assertEquals(10, cartCaptor.getValue().getCount());
    }

    @Test
    public void testAddCart_mergeOverStock() {
        // 准备参数：本次数量、已有数量都未超过库存，但合并后超过
        Long userId = 1L;
        AppCartAddReqVO reqVO = new AppCartAddReqVO().setSkuId(10L).setCount(3);
        // mock 数据
        CartDO dbCart = new CartDO().setId(100L).setUserId(userId).setSpuId(20L).setSkuId(10L)
                .setCount(8).setSelected(false);
        when(cartMapper.selectByUserIdAndSkuId(eq(userId), eq(10L))).thenReturn(dbCart);
        // mock 方法
        when(productSkuApi.getSku(eq(10L))).thenReturn(new ProductSkuRespDTO().setId(10L).setSpuId(20L).setStock(10));

        // 调用，并断言异常
        assertServiceException(() -> cartService.addCart(userId, reqVO), SKU_STOCK_NOT_ENOUGH);
        // 断言：未写入，且原购物项未被修改
        verify(cartMapper, never()).updateById(any(CartDO.class));
        verify(cartMapper, never()).insert(any(CartDO.class));
        assertEquals(8, dbCart.getCount());
        assertFalse(dbCart.getSelected());
    }

    @Test
    public void testAddCart_skuNotExists() {
        // 准备参数
        Long userId = 1L;
        AppCartAddReqVO reqVO = new AppCartAddReqVO().setSkuId(10L).setCount(1);
        // mock 数据
        CartDO dbCart = new CartDO().setId(100L).setUserId(userId).setSpuId(20L).setSkuId(10L)
                .setCount(1).setSelected(true);
        when(cartMapper.selectByUserIdAndSkuId(eq(userId), eq(10L))).thenReturn(dbCart);
        // mock 方法：SKU 已被删除
        when(productSkuApi.getSku(eq(10L))).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(() -> cartService.addCart(userId, reqVO), SKU_NOT_EXISTS);
        // 断言：未写入
        verify(cartMapper, never()).updateById(any(CartDO.class));
        verify(cartMapper, never()).insert(any(CartDO.class));
    }

    @Test
    public void testUpdateCartCount_equalsStock() {
        // 准备参数
        Long userId = 1L;
        AppCartUpdateCountReqVO reqVO = new AppCartUpdateCountReqVO().setId(100L).setCount(10);
        // mock 数据
        when(cartMapper.selectById(eq(100L), eq(userId))).thenReturn(new CartDO().setId(100L).setUserId(userId)
                .setSpuId(20L).setSkuId(10L).setCount(3).setSelected(true));
        // mock 方法
        when(productSkuApi.getSku(eq(10L))).thenReturn(new ProductSkuRespDTO().setId(10L).setSpuId(20L).setStock(10));

        // 调用
        cartService.updateCartCount(userId, reqVO);
        // 断言
        ArgumentCaptor<CartDO> cartCaptor = ArgumentCaptor.forClass(CartDO.class);
        verify(cartMapper).updateById(cartCaptor.capture());
        assertEquals(100L, cartCaptor.getValue().getId());
        assertEquals(10, cartCaptor.getValue().getCount());
    }

    @Test
    public void testUpdateCartCount_overStock() {
        // 准备参数
        Long userId = 1L;
        AppCartUpdateCountReqVO reqVO = new AppCartUpdateCountReqVO().setId(100L).setCount(11);
        // mock 数据
        when(cartMapper.selectById(eq(100L), eq(userId))).thenReturn(new CartDO().setId(100L).setUserId(userId)
                .setSpuId(20L).setSkuId(10L).setCount(3).setSelected(true));
        // mock 方法
        when(productSkuApi.getSku(eq(10L))).thenReturn(new ProductSkuRespDTO().setId(10L).setSpuId(20L).setStock(10));

        // 调用，并断言异常
        assertServiceException(() -> cartService.updateCartCount(userId, reqVO), SKU_STOCK_NOT_ENOUGH);
        // 断言：未写入
        verify(cartMapper, never()).updateById(any(CartDO.class));
    }

}
