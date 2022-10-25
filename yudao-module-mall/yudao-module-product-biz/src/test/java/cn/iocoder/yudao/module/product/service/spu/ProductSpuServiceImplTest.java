package cn.iocoder.yudao.module.product.service.spu;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.property.ProductPropertyRespVO;
import cn.iocoder.yudao.module.product.controller.admin.property.vo.value.ProductPropertyValueRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuCreateOrUpdateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.spu.vo.AppSpuPageRespVO;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;

// TODO @芋艿：review 下单元测试
/**
 * {@link ProductSpuServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
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
        createReqVO.setMaxPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        createReqVO.setMinPrice(CollectionUtils.getMinValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        createReqVO.setTotalStock(CollectionUtils.getSumValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));

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
        reqVO.setMaxPrice(CollectionUtils.getMaxValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        reqVO.setMinPrice(CollectionUtils.getMinValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getPrice));
        reqVO.setTotalStock(CollectionUtils.getSumValue(skuCreateReqList, ProductSkuCreateOrUpdateReqVO::getStock, Integer::sum));

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
    void getSpuDetail() {
        // 准备spu参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class, o -> {
            o.setSpecType(ProductSpuSpecTypeEnum.DISABLE.getType());
        });
        productSpuMapper.insert(createReqVO);

        // 创建两个属性
        ArrayList<ProductPropertyRespVO> productPropertyRespVOS = Lists.newArrayList(
                randomPojo(ProductPropertyRespVO.class),
                randomPojo(ProductPropertyRespVO.class));

        // 所有属性值
        ArrayList<ProductPropertyValueRespVO> productPropertyValueRespVO = new ArrayList<>();

        // 每个属性创建属性值
        productPropertyRespVOS.forEach(v -> {
            ProductPropertyValueRespVO productPropertyValueRespVO1 = randomPojo(ProductPropertyValueRespVO.class, o -> o.setPropertyId(v.getId()));
            productPropertyValueRespVO.add(productPropertyValueRespVO1);
        });

        // 属性值建立笛卡尔积
        Map<Long, List<ProductPropertyValueRespVO>> collect = productPropertyValueRespVO.stream().collect(Collectors.groupingBy(ProductPropertyValueRespVO::getPropertyId));
        List<List<ProductPropertyValueRespVO>> lists = cartesianProduct(Lists.newArrayList(collect.values()));

        // 准备sku参数
        ArrayList<ProductSkuDO> productSkuDOS = Lists.newArrayList();
        lists.forEach(pp -> {
            List<ProductSkuDO.Property> property = pp.stream().map(ppv -> new ProductSkuDO.Property(ppv.getPropertyId(), ppv.getId())).collect(Collectors.toList());
            ProductSkuDO productSkuDO = randomPojo(ProductSkuDO.class, o -> {
                o.setProperties(property);
            });
            productSkuDOS.add(productSkuDO);

        });

        Mockito.when(productSkuService.getSkusBySpuId(createReqVO.getId())).thenReturn(productSkuDOS);
        Mockito.when(productPropertyValueService.getPropertyValueListByPropertyId(new ArrayList<>(collect.keySet()))).thenReturn(productPropertyValueRespVO);
        Mockito.when(productPropertyService.getPropertyList(new ArrayList<>(collect.keySet()))).thenReturn(productPropertyRespVOS);

        // 调用
        ProductSpuDetailRespVO spuDetail = productSpuService.getSpuDetail(createReqVO.getId());

        assertPojoEquals(createReqVO, spuDetail);
    }

    @Test
    void getSpu() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class);
        productSpuMapper.insert(createReqVO);

        ProductSpuRespVO spu = productSpuService.getSpu(createReqVO.getId());
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

    // TODO @luowenfeng：单测要分情况；类似你这个，可以分 2 个单测；一个是有预存预警的；一个是没库存预警的；
    // 然后，参考其它模块的 getPage 类型的方法的单测。
    @Test
    void getSpuPage() {
        // 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class);
        productSpuMapper.insert(createReqVO);

        ArrayList<ProductSkuDO> remindSpuIds = Lists.newArrayList(
//                randomPojo(ProductSkuDO.class, o -> o.setSpuId(createReqVO.getId())),
//                randomPojo(ProductSkuDO.class, o -> o.setSpuId(createReqVO.getId()))
        );

        Mockito.when(productSkuService.getRemindSpuIds()).thenReturn(remindSpuIds);

        // 调用
        ProductSpuPageReqVO productSpuPageReqVO = new ProductSpuPageReqVO();
        productSpuPageReqVO.setTabStatus(2);

        PageResult<ProductSpuRespVO> spuPage = productSpuService.getSpuPage(productSpuPageReqVO);

        ArrayList<Long> resultRemindSpuIds = new ArrayList<>();
        resultRemindSpuIds.add(null);
        PageResult<ProductSpuRespVO> result = ProductSpuConvert.INSTANCE.convertPage(productSpuMapper.selectPage(productSpuPageReqVO, resultRemindSpuIds));
        Assertions.assertIterableEquals(result.getList(), spuPage.getList());
        Assertions.assertEquals(spuPage.getTotal(), result.getTotal());
    }

    @Test
    void testGetSpuPage() {
// 准备参数
        ProductSpuDO createReqVO = randomPojo(ProductSpuDO.class, o -> {
            o.setCategoryId(2L);
        });
        productSpuMapper.insert(createReqVO);

        ArrayList<ProductSkuDO> remindSpuIds = Lists.newArrayList(
//                randomPojo(ProductSkuDO.class, o -> o.setSpuId(createReqVO.getId())),
//                randomPojo(ProductSkuDO.class, o -> o.setSpuId(createReqVO.getId()))
        );

        Mockito.when(productSkuService.getRemindSpuIds()).thenReturn(remindSpuIds);

        // 调用
        AppSpuPageReqVO appSpuPageReqVO = new AppSpuPageReqVO();
        appSpuPageReqVO.setCategoryId(2L);

        PageResult<AppSpuPageRespVO> spuPage = productSpuService.getSpuPage(appSpuPageReqVO);

        PageResult<ProductSpuDO> result = productSpuMapper.selectPage(
                ProductSpuConvert.INSTANCE.convert(appSpuPageReqVO));

        List<AppSpuPageRespVO> collect = result.getList()
                .stream()
                .map(ProductSpuConvert.INSTANCE::convertAppResp)
                .collect(Collectors.toList());

        Assertions.assertIterableEquals(collect, spuPage.getList());
        Assertions.assertEquals(spuPage.getTotal(), result.getTotal());
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

}
