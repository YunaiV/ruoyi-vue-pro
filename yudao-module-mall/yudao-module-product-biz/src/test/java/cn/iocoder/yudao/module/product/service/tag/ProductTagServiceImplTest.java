package cn.iocoder.yudao.module.product.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.tag.vo.ProductTagUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.tag.ProductTagDO;
import cn.iocoder.yudao.module.product.dal.mysql.tag.ProductTagMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.TAG_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

@Import(ProductTagServiceImpl.class)
public class ProductTagServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductTagServiceImpl tagService;

    @Resource
    private ProductTagMapper tagMapper;

    @Test
    public void testCreateTag_success() {
        // 准备参数
        ProductTagCreateReqVO reqVO = randomPojo(ProductTagCreateReqVO.class);

        // 调用
        Long tagId = tagService.createTag(reqVO);
        // 断言
        assertNotNull(tagId);
        // 校验记录的属性是否正确
        ProductTagDO tag = tagMapper.selectById(tagId);
        assertPojoEquals(reqVO, tag);
    }

    @Test
    public void testUpdateTag_success() {
        // mock 数据
        ProductTagDO dbTag = randomPojo(ProductTagDO.class);
        tagMapper.insert(dbTag);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProductTagUpdateReqVO reqVO = randomPojo(ProductTagUpdateReqVO.class, o -> {
            o.setId(dbTag.getId()); // 设置更新的 ID
        });

        // 调用
        tagService.updateTag(reqVO);
        // 校验是否更新正确
        ProductTagDO tag = tagMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, tag);
    }

    @Test
    public void testUpdateTag_notExists() {
        // 准备参数
        ProductTagUpdateReqVO reqVO = randomPojo(ProductTagUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> tagService.updateTag(reqVO), TAG_NOT_EXISTS);
    }

    @Test
    public void testDeleteTag_success() {
        // mock 数据
        ProductTagDO dbTag = randomPojo(ProductTagDO.class);
        tagMapper.insert(dbTag);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbTag.getId();

        // 调用
        tagService.deleteTag(id);
        // 校验数据不存在了
        assertNull(tagMapper.selectById(id));
    }

    @Test
    public void testDeleteTag_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> tagService.deleteTag(id), TAG_NOT_EXISTS);
    }

    @Test
    public void testGetTagPage() {
        // mock 数据
        ProductTagDO dbTag = randomPojo(ProductTagDO.class, o -> { // 等会查询到
            o.setName("test");
            o.setCreateTime(buildTime(2023, 2, 18));
        });
        tagMapper.insert(dbTag);
        // 测试 name 不匹配
        tagMapper.insert(cloneIgnoreId(dbTag, o -> o.setName("ne")));
        // 测试 createTime 不匹配
        tagMapper.insert(cloneIgnoreId(dbTag, o -> o.setCreateTime(null)));
        // 准备参数
        ProductTagPageReqVO reqVO = new ProductTagPageReqVO();
        reqVO.setName("test");
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<ProductTagDO> pageResult = tagService.getTagPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTag, pageResult.getList().get(0));
    }

}
