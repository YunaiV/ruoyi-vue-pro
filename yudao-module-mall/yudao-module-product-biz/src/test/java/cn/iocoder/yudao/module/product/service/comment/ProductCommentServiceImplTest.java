package cn.iocoder.yudao.module.product.service.comment;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.mysql.comment.ProductCommentMapper;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import java.util.Date;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

// TODO 芋艿：单测详细 review 下
/**
 * {@link ProductCommentServiceImpl} 的单元测试类
 *
 * @author wangzhs
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(ProductCommentServiceImpl.class)
public class ProductCommentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductCommentMapper productCommentMapper;

    @Resource
    @Lazy
    private ProductCommentServiceImpl productCommentService;

    @MockBean
    private ProductSpuService productSpuService;
    @MockBean
    private ProductSkuService productSkuService;

    public String generateNo() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomInt(100000, 999999);
    }

    public Long generateId() {
        return RandomUtil.randomLong(100000, 999999);
    }

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

    @Test
    public void testUpdateCommentVisible_success() {
        // mock 测试
        ProductCommentDO productComment = randomPojo(ProductCommentDO.class, o -> {
            o.setVisible(Boolean.TRUE);
        });
        productCommentMapper.insert(productComment);

        Long productCommentId = productComment.getId();

        ProductCommentUpdateVisibleReqVO updateReqVO = new ProductCommentUpdateVisibleReqVO();
        updateReqVO.setId(productCommentId);
        updateReqVO.setVisible(Boolean.FALSE);
        productCommentService.updateCommentVisible(updateReqVO);

        ProductCommentDO productCommentDO = productCommentMapper.selectById(productCommentId);
        assertFalse(productCommentDO.getVisible());
    }


    @Test
    public void testCommentReply_success() {
        // mock 测试
        ProductCommentDO productComment = randomPojo(ProductCommentDO.class);
        productCommentMapper.insert(productComment);

        Long productCommentId = productComment.getId();

        ProductCommentReplyReqVO replyVO = new ProductCommentReplyReqVO();
        replyVO.setId(productCommentId);
        replyVO.setReplyContent("测试");
        productCommentService.replyComment(replyVO, 1L);

        ProductCommentDO productCommentDO = productCommentMapper.selectById(productCommentId);
        assertEquals("测试", productCommentDO.getReplyContent());
    }

}
