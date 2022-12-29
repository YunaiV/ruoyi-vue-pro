package cn.iocoder.yudao.module.product.service.spu;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuRespVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSpuUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppProductSpuPageReqVO;
import cn.iocoder.yudao.module.product.convert.spu.ProductSpuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.brand.ProductBrandServiceImpl;
import cn.iocoder.yudao.module.product.service.category.ProductCategoryServiceImpl;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuServiceImpl;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO @芋艿：review 下单元测试

/**
 * {@link ProductSpuServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ProductSpuServiceImpl.class)
@Disabled // TODO 芋艿：临时去掉
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

    @Test
    public void testCreateSpu_success() {
        // 准备参数
        ProductSpuCreateReqVO createReqVO = randomPojo(ProductSpuCreateReqVO.class, o -> {
            o.setSpecType(ProductSpuSpecTypeEnum.DISABLE.getType());
            o.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        });

        // 校验SKU
        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = createReqVO.getSkus();

        Long spu = productSpuService.createSpu(createReqVO);
        ProductSpuDO productSpuDO = productSpuMapper.selectById(spu);

        createReqVO.setMarketPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getMarketPrice));
//        createReqVO.setMaxPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
//        createReqVO.setMinPrice(CollectionUtils.getMinValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
//        createReqVO.setTotalStock(CollectionUtils.getSumValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));

        assertPojoEquals(createReqVO, productSpuDO);

    }

    @Test
    public void testUpdateSpu_success() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class);
        productSpuMapper.insert(createReqVO);
        // 准备参数
        ProductSpuUpdateReqVO reqVO = randomPojo(ProductSpuUpdateReqVO.class, o -> {
            o.setId(createReqVO.getId()); // 设置更新的 ID
            o.setSpecType(ProductSpuSpecTypeEnum.DISABLE.getType());
            o.setStatus(ProductSpuStatusEnum.DISABLE.getStatus());
        });
        // 调用
        productSpuService.updateSpu(reqVO);

        List<ProductSkuCreateOrUpdateReqVO> skuCreateReqList = reqVO.getSkus();
        reqVO.setMarketPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getMarketPrice));
//        reqVO.setMaxPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
//        reqVO.setMinPrice(CollectionUtils.getMinValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
//        reqVO.setTotalStock(CollectionUtils.getSumValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));

        // 校验是否更新正确
        ProductSpuDO spu = productSpuMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, spu);
    }

    @Test
    public void testValidateSpuExists_exception() {
        ProductSpuUpdateReqVO reqVO = randomPojo(ProductSpuUpdateReqVO.class, o -> {
            o.setSpecType(ProductSpuSpecTypeEnum.DISABLE.getType());
            o.setStatus(ProductSpuStatusEnum.DISABLE.getStatus());
        });
        // 调用
        Assertions.assertThrows(ServiceException.class, () -> productSpuService.updateSpu(reqVO));
    }

    @Test
    void deleteSpu() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class);
        productSpuMapper.insert(createReqVO);

        // 调用
        productSpuService.deleteSpu(createReqVO.getId());

        Assertions.assertNull(productSpuMapper.selectById(createReqVO.getId()));
    }

    @Test
    void getSpu() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class);
        productSpuMapper.insert(createReqVO);

        ProductSpuDO spu = productSpuService.getSpu(createReqVO.getId());
        assertPojoEquals(createReqVO, spu);
    }

    @Test
    void getSpuList() {
        // 准备参数
        ArrayList<ProductSpuDO> createReqVO = Lists.newArrayList(randomPojo(ProductSpuDO.class), randomPojo(ProductSpuDO.class));
        productSpuMapper.insertBatch(createReqVO);

        // 调用
        List<ProductSpuDO> spuList = productSpuService.getSpuList(createReqVO.stream().map(ProductSpuDO::getId).collect(Collectors.toList()));
        Assertions.assertIterableEquals(createReqVO, spuList);
    }

    @Test
    void getSpuPage_alarmStock_empty() {
        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setAlarmStock(true);

        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);

        PageResult<Object> result = PageResult.empty();
        Assertions.assertIterableEquals(result.getList(), spuPage.getList());
        assertEquals(spuPage.getTotal(), result.getTotal());
    }

    @Test
    void getSpuPage_alarmStock() {
        // mock 数据
        Long brandId = generateId();
        Long categoryId = generateId();
        String code = generateNo();

        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class, o->{
            o.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
            o.setTotalStock(500);
            o.setMinPrice(1);
            o.setMaxPrice(50);
            o.setMarketPrice(25);
            o.setSpecType(ProductSpuSpecTypeEnum.RECYCLE.getType());
            o.setBrandId(brandId);
            o.setCategoryId(categoryId);
            o.setClickCount(100);
            o.setCode(code);
            o.setDescription("测试商品");
            o.setPicUrls(new ArrayList<>());
            o.setName("测试");
            o.setSalesCount(100);
            o.setSellPoint("超级加倍");
            o.setShowStock(true);
            o.setVideoUrl("");
        });
        productSpuMapper.insert(createReqVO);

        Set<Long> alarmStockSpuIds = SetUtils.asSet(createReqVO.getId());

        List<ProductSkuDO> productSpuDOS = Arrays.asList(randomPojo(ProductSkuDO.class, o -> {
            o.setSpuId(createReqVO.getId());
        }), randomPojo(ProductSkuDO.class, o -> {
            o.setSpuId(createReqVO.getId());
        }));

        Mockito.when(productSkuService.getSkuListByAlarmStock()).thenReturn(productSpuDOS);

        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setAlarmStock(true);
        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);

        PageResult<ProductSpuRespVO> result = ProductSpuConvert.INSTANCE.convertPage(productSpuMapper.selectPage(productSpuPageReqVO, alarmStockSpuIds));
        Assertions.assertIterableEquals(result.getList(), spuPage.getList());
        assertEquals(spuPage.getTotal(), result.getTotal());
    }

    @Test
    void getSpuPage() {
        // mock 数据
        Long brandId = generateId();
        Long categoryId = generateId();

        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class, o->{
            o.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
            o.setTotalStock(1);
            o.setMinPrice(1);
            o.setMaxPrice(1);
            o.setMarketPrice(1);
            o.setSpecType(ProductSpuSpecTypeEnum.RECYCLE.getType());
            o.setBrandId(brandId);
            o.setCategoryId(categoryId);
            o.setClickCount(1);
            o.setCode(generateNo());
            o.setDescription("测试商品");
            o.setPicUrls(new ArrayList<>());
            o.setName("测试");
            o.setSalesCount(1);
            o.setSellPoint("卖点");
            o.setShowStock(true);
        });

        // 准备参数
        productSpuMapper.insert(createReqVO);
        // 测试 status 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setStatus(ProductSpuStatusEnum.DISABLE.getStatus())));
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setStatus(ProductSpuStatusEnum.RECYCLE.getStatus())));
        // 测试 SpecType 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setSpecType(ProductSpuSpecTypeEnum.DISABLE.getType())));
        // 测试 BrandId 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setBrandId(generateId())));
        // 测试 CategoryId 不匹配
        productSpuMapper.insert(cloneIgnoreId(createReqVO, o -> o.setCategoryId(generateId())));

        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setAlarmStock(false);
        productSpuPageReqVO.setBrandId(brandId);
        productSpuPageReqVO.setStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        productSpuPageReqVO.setCategoryId(categoryId);

        PageResult<ProductSpuDO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);

        PageResult<ProductSpuRespVO> result = ProductSpuConvert.INSTANCE.convertPage(productSpuMapper.selectPage(productSpuPageReqVO, (Set<Long>) null));
        assertEquals(result, spuPage);
    }

    @Test
    void testGetSpuPage() {
// 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class, o -> {
            o.setCategoryId(2L);
        });
        productSpuMapper.insert(createReqVO);

        // 调用
        AppProductSpuPageReqVO appSpuPageReqVO = new AppProductSpuPageReqVO();
        appSpuPageReqVO.setCategoryId(2L);

//        PageResult<AppSpuPageItemRespVO> spuPage = productSpuService.getSpuPage(appSpuPageReqVO);
//
//        PageResult<ProductSpuDO> result = productSpuMapper.selectPage(
//                ProductSpuConvert.INSTANCE.convert(appSpuPageReqVO));
//
//        List<AppSpuPageItemRespVO> collect = result.getList()
//                .stream()
//                .map(ProductSpuConvert.INSTANCE::convertAppResp)
//                .collect(Collectors.toList());
//
//        Assertions.assertIterableEquals(collect, spuPage.getList());
//        assertEquals(spuPage.getTotal(), result.getTotal());
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
        productSpuMapper.insert(randomPojo(ProductSpuDO.class, o -> o.setId(1L).setTotalStock(20)));
        productSpuMapper.insert(randomPojo(ProductSpuDO.class, o -> o.setId(2L).setTotalStock(30)));

        // 调用
        productSpuService.updateSpuStock(stockIncrCounts);
        // 断言
        assertEquals(productSpuService.getSpu(1L).getTotalStock(), 30);
        assertEquals(productSpuService.getSpu(2L).getTotalStock(), 10);
    }

}
