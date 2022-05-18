package cn.iocoder.yudao.module.product.service.spu;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.SpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.spu.SpuMapper;
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
* {@link SpuServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SpuServiceImpl.class)
public class SpuServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SpuServiceImpl spuService;

    @Resource
    private SpuMapper spuMapper;

    @Test
    public void testCreateSpu_success() {
        // 准备参数
        SpuCreateReqVO reqVO = randomPojo(SpuCreateReqVO.class);

        // 调用
        Integer spuId = spuService.createSpu(reqVO);
        // 断言
        assertNotNull(spuId);
        // 校验记录的属性是否正确
        SpuDO spu = spuMapper.selectById(spuId);
        assertPojoEquals(reqVO, spu);
    }

    @Test
    public void testUpdateSpu_success() {
        // mock 数据
        SpuDO dbSpu = randomPojo(SpuDO.class);
        spuMapper.insert(dbSpu);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SpuUpdateReqVO reqVO = randomPojo(SpuUpdateReqVO.class, o -> {
            o.setId(dbSpu.getId()); // 设置更新的 ID
        });

        // 调用
        spuService.updateSpu(reqVO);
        // 校验是否更新正确
        SpuDO spu = spuMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, spu);
    }

    @Test
    public void testUpdateSpu_notExists() {
        // 准备参数
        SpuUpdateReqVO reqVO = randomPojo(SpuUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> spuService.updateSpu(reqVO), SPU_NOT_EXISTS);
    }

    @Test
    public void testDeleteSpu_success() {
        // mock 数据
        SpuDO dbSpu = randomPojo(SpuDO.class);
        spuMapper.insert(dbSpu);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbSpu.getId();

        // 调用
        spuService.deleteSpu(id);
       // 校验数据不存在了
       assertNull(spuMapper.selectById(id));
    }

    @Test
    public void testDeleteSpu_notExists() {
        // 准备参数
        Integer id = 1;

        // 调用, 并断言异常
        assertServiceException(() -> spuService.deleteSpu(id), SPU_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSpuPage() {
       // mock 数据
       SpuDO dbSpu = randomPojo(SpuDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setSellPoint(null);
           o.setDescription(null);
           o.setCategoryId(null);
           o.setPicUrls(null);
           o.setSort(null);
           o.setLikeCount(null);
           o.setPrice(null);
           o.setQuantity(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       spuMapper.insert(dbSpu);
       // 测试 name 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setName(null)));
       // 测试 sellPoint 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setSellPoint(null)));
       // 测试 description 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setDescription(null)));
       // 测试 categoryId 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setCategoryId(null)));
       // 测试 picUrls 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setPicUrls(null)));
       // 测试 sort 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setSort(null)));
       // 测试 likeCount 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setLikeCount(null)));
       // 测试 price 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setPrice(null)));
       // 测试 quantity 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setQuantity(null)));
       // 测试 status 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setCreateTime(null)));
       // 准备参数
       SpuPageReqVO reqVO = new SpuPageReqVO();
       reqVO.setName(null);
       reqVO.setSellPoint(null);
       reqVO.setDescription(null);
       reqVO.setCategoryId(null);
       reqVO.setPicUrls(null);
       reqVO.setSort(null);
       reqVO.setLikeCount(null);
       reqVO.setPrice(null);
       reqVO.setQuantity(null);
       reqVO.setStatus(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<SpuDO> pageResult = spuService.getSpuPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSpu, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSpuList() {
       // mock 数据
       SpuDO dbSpu = randomPojo(SpuDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setSellPoint(null);
           o.setDescription(null);
           o.setCategoryId(null);
           o.setPicUrls(null);
           o.setSort(null);
           o.setLikeCount(null);
           o.setPrice(null);
           o.setQuantity(null);
           o.setStatus(null);
           o.setCreateTime(null);
       });
       spuMapper.insert(dbSpu);
       // 测试 name 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setName(null)));
       // 测试 sellPoint 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setSellPoint(null)));
       // 测试 description 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setDescription(null)));
       // 测试 categoryId 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setCategoryId(null)));
       // 测试 picUrls 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setPicUrls(null)));
       // 测试 sort 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setSort(null)));
       // 测试 likeCount 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setLikeCount(null)));
       // 测试 price 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setPrice(null)));
       // 测试 quantity 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setQuantity(null)));
       // 测试 status 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
       spuMapper.insert(cloneIgnoreId(dbSpu, o -> o.setCreateTime(null)));
       // 准备参数
       SpuExportReqVO reqVO = new SpuExportReqVO();
       reqVO.setName(null);
       reqVO.setSellPoint(null);
       reqVO.setDescription(null);
       reqVO.setCategoryId(null);
       reqVO.setPicUrls(null);
       reqVO.setSort(null);
       reqVO.setLikeCount(null);
       reqVO.setPrice(null);
       reqVO.setQuantity(null);
       reqVO.setStatus(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<SpuDO> list = spuService.getSpuList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSpu, list.get(0));
    }

}
