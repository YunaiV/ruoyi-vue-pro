package cn.iocoder.yudao.module.product.service.comment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.mysql.comment.ProductCommentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;

/**
 * {@link ProductCommentServiceImpl} 的单元测试类
 *
 * @author wangzhs
 */
@Import(ProductCommentServiceImpl.class)
public class ProductCommentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductCommentMapper productCommentMapper;

    @Test
    public void testCreateCommentAndGet_success() {
        // mock 测试
        ProductCommentDO productComment = randomPojo(ProductCommentDO.class);
        productCommentMapper.insert(productComment);

        // 断言
        // 校验记录的属性是否正确
        ProductCommentDO comment = productCommentMapper.selectById(productComment.getId());
        assertPojoEquals(productComment, comment);
    }


}
