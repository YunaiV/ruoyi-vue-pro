package cn.iocoder.yudao.module.product.service.spu;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
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
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.RoundingMode;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_EXISTS;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SPU_NOT_RECYCLE;
import static java.util.Collections.singletonList;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link ProductSpuServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ProductSpuServiceImpl.class)
public class ProductSpuServiceImplTest extends BaseDbUnitTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Resource
    private ProductSpuServiceImpl productSpuService;

    @Resource
    private ProductSpuMapper productSpuMapper;

    @MockitoBean
    private ProductSkuServiceImpl productSkuService;
    @MockitoBean
    private ProductCategoryServiceImpl categoryService;
    @MockitoBean
    private ProductBrandServiceImpl brandService;
    @MockitoBean
    private ProductPropertyService productPropertyService;
    @MockitoBean
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
        ProductSkuSaveReqVO skuCreateOrUpdateReqVO = randomSkuSaveReqVO();
        ProductSpuSaveReqVO createReqVO = randomPojo(ProductSpuSaveReqVO.class,o->{
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setSkus(newArrayList(skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO));
        });
        // mock 方法（分类层级）
        when(categoryService.getCategoryLevel(eq(createReqVO.getCategoryId()))).thenReturn(2);

        // 调用
        Long spuId = productSpuService.createSpu(createReqVO);
        // 断言：销量、浏览量为初始值
        ProductSpuDO productSpuDO = productSpuMapper.selectById(spuId);
        assertPojoEquals(createReqVO, productSpuDO, "salesCount", "browseCount");
        assertEquals(ProductSpuStatusEnum.ENABLE.getStatus(), productSpuDO.getStatus());
        assertEquals(0, productSpuDO.getSalesCount());
        assertEquals(0, productSpuDO.getBrowseCount());
        // 断言：基于 SKU 初始化价格、库存
        assertEquals(skuCreateOrUpdateReqVO.getPrice(), productSpuDO.getPrice());
        assertEquals(skuCreateOrUpdateReqVO.getMarketPrice(), productSpuDO.getMarketPrice());
        assertEquals(skuCreateOrUpdateReqVO.getCostPrice(), productSpuDO.getCostPrice());
        assertEquals(skuCreateOrUpdateReqVO.getStock() * 3, productSpuDO.getStock());
        // 断言：创建 SKU
        verify(productSkuService).createSkuList(eq(spuId), eq(createReqVO.getSkus()));
    }

    @Test
    public void testUpdateSpu_success() {
        // mock 数据
        ProductSpuDO dbSpu = randomSpuDO();
        productSpuMapper.insert(dbSpu);
        // 准备参数
        ProductSkuSaveReqVO skuCreateOrUpdateReqVO = randomSkuSaveReqVO();
        ProductSpuSaveReqVO reqVO = randomPojo(ProductSpuSaveReqVO.class, o -> {
            o.setId(dbSpu.getId()); // 设置更新的 ID
            o.setCategoryId(generateId());
            o.setBrandId(generateId());
            o.setSort(RandomUtil.randomInt(1,100)); // 限制排序范围
            o.setGiveIntegral(generaInt()); // 限制范围为正整数
            o.setVirtualSalesCount(generaInt()); // 限制范围为正整数
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
            o.setSkus(newArrayList(skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO,skuCreateOrUpdateReqVO));
        });
        // mock 方法（分类层级）
        when(categoryService.getCategoryLevel(eq(reqVO.getCategoryId()))).thenReturn(2);

        // 调用
        productSpuService.updateSpu(reqVO);
        // 校验是否更新正确：状态保持不变
        ProductSpuDO spu = productSpuMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, spu);
        assertEquals(dbSpu.getStatus(), spu.getStatus());
        assertEquals(skuCreateOrUpdateReqVO.getStock() * 3, spu.getStock());
        // 断言：更新 SKU
        verify(productSkuService).updateSkuList(eq(reqVO.getId()), eq(reqVO.getSkus()));
    }

    @Test
    public void testUpdateSpu_notExists() {
        // 准备参数
        ProductSpuSaveReqVO reqVO = randomPojo(ProductSpuSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> productSpuService.updateSpu(reqVO), SPU_NOT_EXISTS);
    }

    @Test
    public void testValidateDeliveryTypes_success() {
        // 调用，并断言：快递发货、用户自提，及其组合
        assertEquals(Collections.emptySet(), validateDeliveryTypes(singletonList(DeliveryTypeEnum.EXPRESS.getType())));
        assertEquals(Collections.emptySet(), validateDeliveryTypes(singletonList(DeliveryTypeEnum.PICK_UP.getType())));
        assertEquals(Collections.emptySet(), validateDeliveryTypes(Arrays.asList(
                DeliveryTypeEnum.EXPRESS.getType(), DeliveryTypeEnum.PICK_UP.getType())));
    }

    @Test
    public void testValidateDeliveryTypes_invalid() {
        // 调用，并断言：不在枚举范围内
        Set<String> invalidMessages = Collections.singleton("配送方式不正确");
        assertEquals(invalidMessages, validateDeliveryTypes(singletonList(0)));
        assertEquals(invalidMessages, validateDeliveryTypes(singletonList(3)));
        assertEquals(invalidMessages, validateDeliveryTypes(singletonList(-1)));
        assertEquals(invalidMessages, validateDeliveryTypes(Arrays.asList(DeliveryTypeEnum.EXPRESS.getType(), 3)));
        assertEquals(invalidMessages, validateDeliveryTypes(Arrays.asList(DeliveryTypeEnum.EXPRESS.getType(), null)));
        // 调用，并断言：为空
        Set<String> emptyMessages = Collections.singleton("配送方式不能为空");
        assertEquals(emptyMessages, validateDeliveryTypes(null));
        assertEquals(emptyMessages, validateDeliveryTypes(Collections.emptyList()));
    }

    @Test
    public void testDeleteSpu_success() {
        // mock 数据
        ProductSpuDO dbSpu = randomSpuDO(o -> o.setStatus(ProductSpuStatusEnum.RECYCLE.getStatus())); // 加入回收站才可删除
        productSpuMapper.insert(dbSpu);

        // 调用
        productSpuService.deleteSpu(dbSpu.getId());
        // 断言
        assertNull(productSpuMapper.selectById(dbSpu.getId()));
        verify(productSkuService).deleteSkuBySpuId(eq(dbSpu.getId()));
    }

    @Test
    public void testDeleteSpu_notRecycle() {
        // mock 数据
        ProductSpuDO dbSpu = randomSpuDO(o -> o.setStatus(ProductSpuStatusEnum.DISABLE.getStatus()));
        productSpuMapper.insert(dbSpu);

        // 调用, 并断言异常
        assertServiceException(() -> productSpuService.deleteSpu(dbSpu.getId()), SPU_NOT_RECYCLE);
        // 断言
        assertNotNull(productSpuMapper.selectById(dbSpu.getId()));
        verify(productSkuService, never()).deleteSkuBySpuId(any());
    }

    @Test
    public void testGetSpu() {
        // mock 数据
        ProductSpuDO dbSpu = randomSpuDO();
        productSpuMapper.insert(dbSpu);

        // 调用
        ProductSpuDO spu = productSpuService.getSpu(dbSpu.getId());
        // 断言
        assertPojoEquals(dbSpu, spu);
    }

    @Test
    public void testGetSpuList() {
        // mock 数据
        ArrayList<ProductSpuDO> dbSpus = Lists.newArrayList(randomSpuDO(), randomSpuDO());
        productSpuMapper.insertBatch(dbSpus);
        // 准备参数：倒序传入
        List<Long> ids = Arrays.asList(dbSpus.get(1).getId(), dbSpus.get(0).getId());

        // 调用
        List<ProductSpuDO> spuList = productSpuService.getSpuList(ids);
        // 断言：按照 ids 顺序返回
        assertEquals(ids, convertList(spuList, ProductSpuDO::getId));
        assertPojoEquals(dbSpus.get(1), spuList.get(0));
        assertPojoEquals(dbSpus.get(0), spuList.get(1));
    }

    @Test
    public void testGetSpuPage_alertStockEmpty() {
        // mock 数据
        ArrayList<ProductSpuDO> dbSpus = Lists.newArrayList(randomSpuDO(o -> o.setStock(11)),
                randomSpuDO(o -> o.setStock(11)));
        productSpuMapper.insertBatch(dbSpus);
        // 准备参数
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setTabType(ProductSpuPageReqVO.ALERT_STOCK);

        // 调用
        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);
        // 断言
        assertEquals(0, spuPage.getTotal());
        assertEquals(0, spuPage.getList().size());
    }

    @Test
    public void testGetSpuPage_alertStock() {
        // mock 数据
        ArrayList<ProductSpuDO> dbSpus = Lists.newArrayList(randomSpuDO(o -> o.setStock(5)),
                randomSpuDO(o -> o.setStock(9)));
        productSpuMapper.insertBatch(dbSpus);
        // 测试 stock 不匹配
        productSpuMapper.insert(randomSpuDO(o -> o.setStock(11)));
        // 测试 status 不匹配（回收站）
        productSpuMapper.insert(randomSpuDO(o -> o.setStock(5).setStatus(ProductSpuStatusEnum.RECYCLE.getStatus())));
        // 准备参数
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setTabType(ProductSpuPageReqVO.ALERT_STOCK);

        // 调用
        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);
        // 断言
        assertEquals(dbSpus.size(), spuPage.getTotal());
        assertEquals(convertSet(dbSpus, ProductSpuDO::getId), convertSet(spuPage.getList(), ProductSpuDO::getId));
    }

    @Test
    public void testGetSpuPage() {
        // mock 数据
        ProductSpuDO dbSpu = randomSpuDO(o -> { // 等会查询到
            o.setName("芋道商品");
            o.setCategoryId(1L);
            o.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2023, 2, 10));
        });
        productSpuMapper.insert(dbSpu);
        // 测试 name 不匹配
        productSpuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setName("土豆商品")));
        // 测试 categoryId 不匹配
        productSpuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setCategoryId(2L)));
        // 测试 createTime 不匹配
        productSpuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setCreateTime(buildTime(2023, 3, 10))));
        // 测试 status 不匹配（出售中）
        productSpuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setStatus(ProductSpuStatusEnum.DISABLE.getStatus())));
        productSpuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setStatus(ProductSpuStatusEnum.RECYCLE.getStatus())));
        // 准备参数
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setName("芋道");
        productSpuPageReqVO.setCategoryId(1L);
        productSpuPageReqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
        productSpuPageReqVO.setTabType(ProductSpuPageReqVO.FOR_SALE);

        // 调用
        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);
        // 断言
        assertEquals(1, spuPage.getTotal());
        assertEquals(1, spuPage.getList().size());
        assertPojoEquals(dbSpu, spuPage.getList().get(0));
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
        productSpuMapper.insert(randomSpuDO(o -> o.setId(1L).setStock(20)));
        productSpuMapper.insert(randomSpuDO(o -> o.setId(2L).setStock(30)));

        // 调用
        productSpuService.updateSpuStock(stockIncrCounts);
        // 断言
        assertEquals(productSpuService.getSpu(1L).getStock(), 30);
        assertEquals(productSpuService.getSpu(2L).getStock(), 10);
    }

    /**
     * 校验 {@link ProductSpuSaveReqVO#getDeliveryTypes()} 字段
     *
     * @param deliveryTypes 配送方式数组
     * @return 校验不通过的提示
     */
    private static Set<String> validateDeliveryTypes(List<Integer> deliveryTypes) {
        ProductSpuSaveReqVO reqVO = new ProductSpuSaveReqVO();
        reqVO.setDeliveryTypes(deliveryTypes);
        Set<ConstraintViolation<ProductSpuSaveReqVO>> violations = VALIDATOR.validateProperty(reqVO, "deliveryTypes");
        return convertSet(violations, ConstraintViolation::getMessage);
    }

    // ========== 随机对象 ==========

    private ProductSkuSaveReqVO randomSkuSaveReqVO() {
        return randomPojo(ProductSkuSaveReqVO.class, o->{
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
    }

    @SafeVarargs
    private final ProductSpuDO randomSpuDO(Consumer<ProductSpuDO>... consumers) {
        Consumer<ProductSpuDO> consumer = o -> {
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
            o.setSalesCount(generaInt()); // 限制范围为正整数
            o.setBrowseCount(generaInt()); // 限制范围为正整数
        };
        return randomPojo(ProductSpuDO.class, ArrayUtils.append(consumer, consumers));
    }

}
