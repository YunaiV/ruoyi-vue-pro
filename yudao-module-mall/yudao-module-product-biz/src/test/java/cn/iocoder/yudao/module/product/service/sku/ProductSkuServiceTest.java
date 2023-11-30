package cn.iocoder.yudao.module.product.service.sku;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

/**
 * {@link ProductSkuServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ProductSkuServiceImpl.class)
public class ProductSkuServiceTest extends BaseDbUnitTest {

    @Resource
    private ProductSkuService productSkuService;

    @Resource
    private ProductSkuMapper productSkuMapper;

    @MockBean
    private ProductSpuService productSpuService;
    @MockBean
    private ProductPropertyService productPropertyService;
    @MockBean
    private ProductPropertyValueService productPropertyValueService;

    public Long generateId() {
        return RandomUtil.randomLong(100000, 999999);
    }

    @Test
    public void testUpdateSkuList() {
        // mock 数据
        ProductSkuDO sku01 = randomPojo(ProductSkuDO.class, o -> { // 测试更新
            o.setSpuId(1L);
            o.setProperties(singletonList(new ProductSkuDO.Property(
                    10L, "颜色", 20L, "红色")));
        });
        productSkuMapper.insert(sku01);
        ProductSkuDO sku02 = randomPojo(ProductSkuDO.class, o -> { // 测试删除
            o.setSpuId(1L);
            o.setProperties(singletonList(new ProductSkuDO.Property(
                    10L, "颜色", 30L, "蓝色")));

        });
        productSkuMapper.insert(sku02);
        // 准备参数
        Long spuId = 1L;
        String spuName = "测试商品";
        List<ProductSkuCreateOrUpdateReqVO> skus = Arrays.asList(
                randomPojo(ProductSkuCreateOrUpdateReqVO.class, o -> { // 测试更新
                    o.setProperties(singletonList(new ProductSkuCreateOrUpdateReqVO.Property(
                            10L, "颜色", 20L, "红色")));
                }),
                randomPojo(ProductSkuCreateOrUpdateReqVO.class, o -> { // 测试新增
                    o.setProperties(singletonList(new ProductSkuCreateOrUpdateReqVO.Property(
                            10L, "颜色", 20L, "红色")));
                })
        );

        // 调用
        productSkuService.updateSkuList(spuId, skus);
        // 断言
        List<ProductSkuDO> dbSkus = productSkuMapper.selectListBySpuId(spuId);
        assertEquals(dbSkus.size(), 2);
        // 断言更新的
        assertEquals(dbSkus.get(0).getId(), sku01.getId());
        assertPojoEquals(dbSkus.get(0), skus.get(0), "properties");
        assertEquals(skus.get(0).getProperties().size(), 1);
        assertPojoEquals(dbSkus.get(0).getProperties().get(0), skus.get(0).getProperties().get(0));
        // 断言新增的
        assertNotEquals(dbSkus.get(1).getId(), sku02.getId());
        assertPojoEquals(dbSkus.get(1), skus.get(1), "properties");
        assertEquals(skus.get(1).getProperties().size(), 1);
        assertPojoEquals(dbSkus.get(1).getProperties().get(0), skus.get(1).getProperties().get(0));
    }

    @Test
    public void testUpdateSkuStock_incrSuccess() {
        // 准备参数
        ProductSkuUpdateStockReqDTO updateStockReqDTO = new ProductSkuUpdateStockReqDTO()
                .setItems(singletonList(new ProductSkuUpdateStockReqDTO.Item().setId(1L).setIncrCount(10)));
        // mock 数据
        productSkuMapper.insert(randomPojo(ProductSkuDO.class, o -> {
            o.setId(1L).setSpuId(10L).setStock(20);
            o.getProperties().forEach(p -> {
                // 指定 id 范围 解决 Value too long
                p.setPropertyId(generateId());
                p.setValueId(generateId());
            });
        }));

        // 调用
        productSkuService.updateSkuStock(updateStockReqDTO);
        // 断言
        ProductSkuDO sku = productSkuMapper.selectById(1L);
        assertEquals(sku.getStock(), 30);
        verify(productSpuService).updateSpuStock(argThat(spuStockIncrCounts -> {
            assertEquals(spuStockIncrCounts.size(), 1);
            assertEquals(spuStockIncrCounts.get(10L), 10);
            return true;
        }));
    }

    @Test
    public void testUpdateSkuStock_decrSuccess() {
        // 准备参数
        ProductSkuUpdateStockReqDTO updateStockReqDTO = new ProductSkuUpdateStockReqDTO()
                .setItems(singletonList(new ProductSkuUpdateStockReqDTO.Item().setId(1L).setIncrCount(-10)));
        // mock 数据
        productSkuMapper.insert(randomPojo(ProductSkuDO.class, o -> {
            o.setId(1L).setSpuId(10L).setStock(20);
            o.getProperties().forEach(p -> {
                // 指定 id 范围 解决 Value too long
                p.setPropertyId(generateId());
                p.setValueId(generateId());
            });
        }));

        // 调用
        productSkuService.updateSkuStock(updateStockReqDTO);
        // 断言
        ProductSkuDO sku = productSkuMapper.selectById(1L);
        assertEquals(sku.getStock(), 10);
        verify(productSpuService).updateSpuStock(argThat(spuStockIncrCounts -> {
            assertEquals(spuStockIncrCounts.size(), 1);
            assertEquals(spuStockIncrCounts.get(10L), -10);
            return true;
        }));
    }

    @Test
    public void testUpdateSkuStock_decrFail() {
        // 准备参数
        ProductSkuUpdateStockReqDTO updateStockReqDTO = new ProductSkuUpdateStockReqDTO()
                .setItems(singletonList(new ProductSkuUpdateStockReqDTO.Item().setId(1L).setIncrCount(-30)));
        // mock 数据
        productSkuMapper.insert(randomPojo(ProductSkuDO.class, o -> {
            o.setId(1L).setSpuId(10L).setStock(20);
            o.getProperties().forEach(p -> {
                // 指定 id 范围 解决 Value too long
                p.setPropertyId(generateId());
                p.setValueId(generateId());
            });
        }));
        // 调用并断言
        AssertUtils.assertServiceException(() -> productSkuService.updateSkuStock(updateStockReqDTO),
                SKU_STOCK_NOT_ENOUGH);
    }

    @Test
    public void testDeleteSku_success() {
        ProductSkuDO dbSku = randomPojo(ProductSkuDO.class, o -> {
            o.setId(generateId()).setSpuId(generateId());
            o.getProperties().forEach(p -> {
                // 指定 id 范围 解决 Value too long
                p.setPropertyId(generateId());
                p.setValueId(generateId());
            });
        });
        // mock 数据
        productSkuMapper.insert(dbSku);
        // 准备参数
        Long id = dbSku.getId();

        // 调用
        productSkuService.deleteSku(id);
        // 校验数据不存在了
        assertNull(productSkuMapper.selectById(id));
    }

    @Test
    public void testDeleteSku_notExists() {
        // 准备参数
        Long id = 1L;

        // 调用, 并断言异常
        assertServiceException(() -> productSkuService.deleteSku(id), SKU_NOT_EXISTS);
    }
}
