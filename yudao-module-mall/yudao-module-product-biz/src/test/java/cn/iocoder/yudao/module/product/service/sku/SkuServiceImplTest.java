package cn.iocoder.yudao.module.product.service.sku;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.SkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.SkuMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
* {@link SkuServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SkuServiceImpl.class)
public class SkuServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SkuServiceImpl skuService;

    @Resource
    private SkuMapper skuMapper;

    @Test
    public void testCreateSku_success() {
        // 准备参数
        SkuCreateReqVO reqVO = randomPojo(SkuCreateReqVO.class);

        // 调用
        Integer skuId = skuService.createSku(reqVO);
        // 断言
        assertNotNull(skuId);
        // 校验记录的属性是否正确
        SkuDO sku = skuMapper.selectById(skuId);
        assertPojoEquals(reqVO, sku);
    }

    @Test
    public void testUpdateSku_success() {
        // mock 数据
        SkuDO dbSku = randomPojo(SkuDO.class);
        skuMapper.insert(dbSku);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SkuUpdateReqVO reqVO = randomPojo(SkuUpdateReqVO.class, o -> {
            o.setId(dbSku.getId()); // 设置更新的 ID
        });

        // 调用
        skuService.updateSku(reqVO);
        // 校验是否更新正确
        SkuDO sku = skuMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, sku);
    }

    @Test
    public void testUpdateSku_notExists() {
        // 准备参数
        SkuUpdateReqVO reqVO = randomPojo(SkuUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> skuService.updateSku(reqVO), SKU_NOT_EXISTS);
    }

    @Test
    public void testDeleteSku_success() {
        // mock 数据
        SkuDO dbSku = randomPojo(SkuDO.class);
        skuMapper.insert(dbSku);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbSku.getId();

        // 调用
        skuService.deleteSku(id);
       // 校验数据不存在了
       assertNull(skuMapper.selectById(id));
    }

    @Test
    public void testDeleteSku_notExists() {
        // 准备参数
        Integer id = 1;

        // 调用, 并断言异常
        assertServiceException(() -> skuService.deleteSku(id), SKU_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSkuPage() {
       // mock 数据
       SkuDO dbSku = randomPojo(SkuDO.class, o -> { // 等会查询到
           o.setSpuId(null);
           o.setProperties(null);
           o.setPrice(null);
           o.setOriginalPrice(null);
           o.setCostPrice(null);
           o.setBarCode(null);
           o.setPicUrl(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       skuMapper.insert(dbSku);
       // 测试 spuId 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setSpuId(null)));
       // 测试 properties 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setProperties(null)));
       // 测试 price 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setPrice(null)));
       // 测试 originalPrice 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setOriginalPrice(null)));
       // 测试 costPrice 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setCostPrice(null)));
       // 测试 barCode 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setBarCode(null)));
       // 测试 picUrl 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setPicUrl(null)));
       // 测试 status 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setCreateTime(null)));
       // 准备参数
       SkuPageReqVO reqVO = new SkuPageReqVO();
       reqVO.setSpuId(null);
       reqVO.setProperties(null);
       reqVO.setPrice(null);
       reqVO.setOriginalPrice(null);
       reqVO.setCostPrice(null);
       reqVO.setBarCode(null);
       reqVO.setPicUrl(null);
       reqVO.setStatus(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<SkuDO> pageResult = skuService.getSkuPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSku, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSkuList() {
       // mock 数据
       SkuDO dbSku = randomPojo(SkuDO.class, o -> { // 等会查询到
           o.setSpuId(null);
           o.setProperties(null);
           o.setPrice(null);
           o.setOriginalPrice(null);
           o.setCostPrice(null);
           o.setBarCode(null);
           o.setPicUrl(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       skuMapper.insert(dbSku);
       // 测试 spuId 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setSpuId(null)));
       // 测试 properties 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setProperties(null)));
       // 测试 price 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setPrice(null)));
       // 测试 originalPrice 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setOriginalPrice(null)));
       // 测试 costPrice 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setCostPrice(null)));
       // 测试 barCode 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setBarCode(null)));
       // 测试 picUrl 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setPicUrl(null)));
       // 测试 status 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       skuMapper.insert(cloneIgnoreId(dbSku, o -> o.setCreateTime(null)));
       // 准备参数
       SkuExportReqVO reqVO = new SkuExportReqVO();
       reqVO.setSpuId(null);
       reqVO.setProperties(null);
       reqVO.setPrice(null);
       reqVO.setOriginalPrice(null);
       reqVO.setCostPrice(null);
       reqVO.setBarCode(null);
       reqVO.setPicUrl(null);
       reqVO.setStatus(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<SkuDO> list = skuService.getSkuList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSku, list.get(0));
    }

}
