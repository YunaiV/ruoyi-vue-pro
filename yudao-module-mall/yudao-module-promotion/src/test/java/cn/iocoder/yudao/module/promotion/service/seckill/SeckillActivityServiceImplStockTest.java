package cn.iocoder.yudao.module.promotion.service.seckill;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillActivityMapper;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckill.seckillactivity.SeckillProductMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SeckillActivityServiceImpl} 的库存单元测试类
 *
 * @author 芋道源码
 */
public class SeckillActivityServiceImplStockTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SeckillActivityServiceImpl seckillActivityService;

    @Mock
    private SeckillActivityMapper seckillActivityMapper;
    @Mock
    private SeckillProductMapper seckillProductMapper;

    @Test
    public void testUpdateSeckillStockIncr_success() {
        // 准备参数
        Long id = randomLongId();
        Long skuId = randomLongId();
        Integer count = 2;
        // mock 方法
        SeckillProductDO product = randomPojo(SeckillProductDO.class, o -> {
            o.setActivityId(id);
            o.setSkuId(skuId);
        });
        when(seckillProductMapper.selectByActivityIdAndSkuId(eq(id), eq(skuId))).thenReturn(product);

        // 调用
        seckillActivityService.updateSeckillStockIncr(id, skuId, count);
        // 断言
        verify(seckillProductMapper).updateStockIncr(eq(product.getId()), eq(count));
        verify(seckillActivityMapper).updateStockIncr(eq(id), eq(count));
    }

    @Test
    public void testUpdateSeckillStockIncr_notExists() {
        // 准备参数
        Long id = randomLongId();
        Long skuId = randomLongId();
        Integer count = 2;
        // mock 方法（活动已删除，或 SKU 已从活动移除）
        when(seckillProductMapper.selectByActivityIdAndSkuId(eq(id), eq(skuId))).thenReturn(null);

        // 调用
        assertDoesNotThrow(() -> seckillActivityService.updateSeckillStockIncr(id, skuId, count));
        // 断言
        verify(seckillProductMapper, never()).updateStockIncr(anyLong(), anyInt());
        verify(seckillActivityMapper, never()).updateStockIncr(anyLong(), anyInt());
    }

}
