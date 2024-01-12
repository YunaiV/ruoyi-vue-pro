package cn.iocoder.yudao.module.product.service.spu;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuSaveReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandServiceImpl;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryServiceImpl;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuServiceImpl;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// TODO @芋艿：review 下单元测试

/**
 * {@link ProductSpuServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ProductSpuServiceImpl.class)
public class ProductSpuServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductSpuServiceImpl productSpuService;

    @Resource
    private ProductSpuMapper productSpuMapper;

    @MockBean
    private ProductSkuServiceImpl productSkuService;
    @MockBean
    private ProductCategoryServiceImpl categoryService;
    @MockBean
    private ProductBrandServiceImpl brandService;
    @MockBean
    private ProductPropertyService productPropertyService;
    @MockBean
    private ProductPropertyValueService productPropertyValueService;

    public String generateNo() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomInt(100000, 999999);
    }

    public Long generateId() {
        return RandomUtil.randomLong(100000, 999999);
    }

    public int generaInt(){return RandomUtil.randomInt(1,9999999);}

    // TODO @芋艿：单测后续 review 哈

    @Test
    public void testCreateSpu_success() {
        // 准备参数
        ProductSkuSaveReqVO skuCreateOrUpdateReqVO = randomPojo(ProductSkuSaveReqVO.class, o->{
            // 限制范围为正整数
            o.setCostPrice(generaInt());
            o.setPrice(generaInt());
            o.setMarketPrice(generaInt());
            o.setStock(generaInt());
            o.setFirstBrokeragePrice(generaInt());
            o.setSecondBrokeragePrice(generaInt());
            // 限制分数为两位数
            o.setWeight(RandomUtil.randomDouble(10,2, RoundingMode.HALF_UP));
            o.setVolume(RandomUtil.randomDouble(10,2, RoundingMode.HALF_UP));
        });
        ProductSpuSaveReqVO createReqVO = randomPojo(ProductSpuSaveReqVO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setSkus(newArrayList(skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO));
        });
        when(categoryService.getCategoryLevel(eq(createReqVO.getCategoryId()))).thenReturn(2);
        Long spu = productSpuService.createSpu(createReqVO);
        ProductSpuDO productSpuDO = productSpuMapper.selectById(spu);
        assertPojoEquals(createReqVO, productSpuDO);
    }

    @Test
    public void testUpdateSpu_success() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        });
        productSpuMapper.insert(createReqVO);
        // 准备参数
        ProductSkuSaveReqVO skuCreateOrUpdateReqVO = randomPojo(ProductSkuSaveReqVO.class, o->{
            // 限制范围为正整数
            o.setCostPrice(generaInt());
            o.setPrice(generaInt());
            o.setMarketPrice(generaInt());
            o.setStock(generaInt());
            o.setFirstBrokeragePrice(generaInt());
            o.setSecondBrokeragePrice(generaInt());
            // 限制分数为两位数
            o.setWeight(RandomUtil.randomDouble(10,2, RoundingMode.HALF_UP));
            o.setVolume(RandomUtil.randomDouble(10,2, RoundingMode.HALF_UP));
        });
        // 准备参数
        ProductSpuSaveReqVO reqVO = randomPojo(ProductSpuSaveReqVO.class, o -> {
            o.setId(createReqVO.getId()); // 设置更新的 ID
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
            o.setSkus(newArrayList(skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO));
        });
        when(categoryService.getCategoryLevel(eq(reqVO.getCategoryId()))).thenReturn(2);
        // 调用
        productSpuService.updateSpu(reqVO);
        // 校验是否更新正确
        ProductSpuDO spu = productSpuMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, spu);
    }

    @Test
    public void testValidateSpuExists_exception() {
        ProductSpuSaveReqVO reqVO = randomPojo(ProductSpuSaveReqVO.class);
        // 调用
        Assertions.assertThrows(ServiceException.class, () -> productSpuService.updateSpu(reqVO));
    }

    @Test
    void deleteSpu() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
            o.setStatus(-1); // 加入回收站才可删除
        });
        productSpuMapper.insert(createReqVO);

        // 调用
        productSpuService.deleteSpu(createReqVO.getId());

        Assertions.assertNull(productSpuMapper.selectById(createReqVO.getId()));
    }

    @Test
    void getSpu() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        });
        productSpuMapper.insert(createReqVO);

        ProductSpuDO spu = productSpuService.getSpu(createReqVO.getId());
        assertPojoEquals(createReqVO, spu);
    }

    @Test
    void getSpuList() {
        // 准备参数
        ArrayList<ProductSpuDO> createReqVOs = Lists.newArrayList(randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        }), randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        }));
        productSpuMapper.insertBatch(createReqVOs);

        // 调用
        List<ProductSpuDO> spuList = productSpuService.getSpuList(createReqVOs.stream().map(ProductSpuDO::getId).collect(Collectors.toList()));
        Assertions.assertIterableEquals(createReqVOs, spuList);
    }

    @Test
    void getSpuPage_alarmStock_empty() {
        // 准备参数
        ArrayList<ProductSpuDO> createReqVOs = Lists.newArrayList(randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(11); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        }), randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(11); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        }));
        productSpuMapper.insertBatch(createReqVOs);
        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setTabType(ProductSpuPageReqVO.ALERT_STOCK);

        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);

        PageResult<Object> result = PageResult.empty();
        Assertions.assertIterableEquals(result.getList(), spuPage.getList());
        assertEquals(spuPage.getTotal(), result.getTotal());
    }

    @Test
    void getSpuPage_alarmStock() {
        // 准备参数
        ArrayList<ProductSpuDO> createReqVOs = Lists.newArrayList(randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(5); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        }), randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(9); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        }));
        productSpuMapper.insertBatch(createReqVOs);
        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setTabType(ProductSpuPageReqVO.ALERT_STOCK);
        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);
        assertEquals(createReqVOs.size(), spuPage.getTotal());
    }

    @Test
    void testGetSpuPage() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        });

        // 准备参数
        productSpuMapper.insert(createReqVO);
        // 测试 status 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setStatus(ProductSpuStatusEnum.DISABLE.getStatus())));
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setStatus(ProductSpuStatusEnum.RECYCLE.getStatus())));
        // 测试 SpecType 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setSpecType(true)));
        // 测试 BrandId 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setBrandId(generateId())));
        // 测试 CategoryId 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setCategoryId(generateId())));

        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        // 查询条件 按需打开
        //productSpuPageReqVO.setTabType(ProductSpuPageReqVO.ALERT_STOCK);
        //productSpuPageReqVO.setTabType(ProductSpuPageReqVO.RECYCLE_BIN);
        //productSpuPageReqVO.setTabType(ProductSpuPageReqVO.FOR_SALE);
        //productSpuPageReqVO.setTabType(ProductSpuPageReqVO.IN_WAREHOUSE);
        //productSpuPageReqVO.setTabType(ProductSpuPageReqVO.SOLD_OUT);
        //productSpuPageReqVO.setName(createReqVO.getName());
        //productSpuPageReqVO.setCategoryId(createReqVO.getCategoryId());

        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);

        assertEquals(1, spuPage.getTotal());
    }

    /**
     * 生成笛卡尔积
     *
     * @param data 数据
     * @return 笛卡尔积
     */
    public static <T> List<List<T>> cartesianProduct(List<List<T>> data) {
        List<List<T>> res = null; // 结果集(当前为第N个List，则该处存放的就为前N-1个List的笛卡尔积集合)
        for (List<T> list : data) { // 遍历数据
            List<List<T>> temp = new ArrayList<>(); // 临时结果集，存放本次循环后生成的笛卡尔积集合
            if (res == null) { // 结果集为null表示第一次循环既list为第一个List
                for (T t : list) { // 便利第一个List
                    // 利用stream生成List，第一个List的笛卡尔积集合约等于自己本身（需要创建一个List并把对象添加到当中），存放到临时结果集
                    temp.add(Stream.of(t).collect(Collectors.toList()));
                }
                res = temp; // 将临时结果集赋值给结果集
                continue; // 跳过本次循环
            }
            // 不为第一个List，计算前面的集合（笛卡尔积）和当前List的笛卡尔积集合
            for (T t : list) { // 便利
                for (List<T> rl : res) { // 便利前面的笛卡尔积集合
                    // 利用stream生成List
                    temp.add(Stream.concat(rl.stream(), Stream.of(t)).collect(Collectors.toList()));
                }
            }
            res = temp; // 将临时结果集赋值给结果集
        }
        // 返回结果
        return res;
    }

    @Test
    public void testUpdateSpuStock() {
        // 准备参数
        Map<Long, Integer> stockIncrCounts = MapUtil.builder(1L, 10).put(2L, -20).build();
        // mock 方法（数据）
        productSpuMapper.insert(randomPojo(ProductSpuDO.class, o ->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
            o.setId(1L).setStock(20);
        }));
        productSpuMapper.insert(randomPojo(ProductSpuDO.class, o -> {
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setDeliveryTemplateId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setPrice(generaInt()); // 限制范围为正整数
            o.setMarketPrice(generaInt()); // 限制范围为正整数
            o.setCostPrice(generaInt()); // 限制范围为正整数
            o.setStock(generaInt()); // 限制范围为正整数
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
            o.setId(2L).setStock(30);
        }));

        // 调用
        productSpuService.updateSpuStock(stockIncrCounts);
        // 断言
        assertEquals(productSpuService.getSpu(1L).getStock(), 30);
        assertEquals(productSpuService.getSpu(2L).getStock(), 10);
    }

}
