package cn.iocoder.yudao.module.product.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.comment.ProductCommentMapper;
import cn.iocoder.yudao.module.product.enums.comment.ProductCommentScoresEnum;
import cn.iocoder.yudao.module.product.service.sku.ProductSkuService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// TODO 芋艿：单测详细 review 下
/**
 * {@link ProductCommentServiceImpl} 的单元测试类
 *
 * @author wangzhs
 */
// DONE 芋艿：后续 fix 补充的单测
@Import(ProductCommentServiceImpl.class)
public class ProductCommentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductCommentServiceImpl productCommentService;

    @Resource
    private ProductCommentMapper productCommentMapper;

    @MockitoBean
    private ProductSpuService productSpuService;
    @MockitoBean
    private ProductSkuService productSkuService;
    @MockitoBean
    private MemberUserApi memberUserApi;

    // ========== 管理后台添加虚拟评价 ==========

    @Test
    public void testCreateComment_success() {
        // 准备参数
        ProductCommentCreateReqVO reqVO = randomPojo(ProductCommentCreateReqVO.class, o -> o
                .setDescriptionScores(ProductCommentScoresEnum.FIVE.getScores())
                .setBenefitScores(ProductCommentScoresEnum.FOUR.getScores()));
        // mock 方法
        ProductSkuDO sku = mockSku(reqVO.getSkuId());
        ProductSpuDO spu = mockSpu(sku.getSpuId());

        // 调用
        productCommentService.createComment(reqVO);
        // 断言：虚拟评价不关联会员和订单
        List<ProductCommentDO> comments = productCommentMapper.selectList();
        assertEquals(1, comments.size());
        ProductCommentDO comment = comments.get(0);
        assertPojoEquals(reqVO, comment);
        assertEquals(0L, comment.getUserId());
        assertNull(comment.getOrderId());
        assertNull(comment.getOrderItemId());
        assertTrue(comment.getVisible());
        assertFalse(comment.getAnonymous());
        assertEquals(4, comment.getScores()); // (5 + 4) / 2 向下取整
        // 断言：商品信息
        assertEquals(spu.getId(), comment.getSpuId());
        assertEquals(spu.getName(), comment.getSpuName());
        assertEquals(sku.getPicUrl(), comment.getSkuPicUrl());
        assertEquals(sku.getProperties(), comment.getSkuProperties());
    }

    @Test
    public void testCreateComment_ignoreUserIdAndOrderItemId() throws Exception {
        // 准备参数：旧调用方仍传入 userId、orderItemId，使用 Spring MVC 默认的 Jackson2ObjectMapperBuilder 反序列化
        String json = "{\"userId\":968,\"orderItemId\":1024,\"skuId\":1,\"userNickname\":\"芋艿\","
                + "\"userAvatar\":\"https://www.iocoder.cn/1.png\",\"descriptionScores\":5,\"benefitScores\":5,"
                + "\"content\":\"好评\",\"picUrls\":[]}";
        ProductCommentCreateReqVO reqVO = Jackson2ObjectMapperBuilder.json().build()
                .readValue(json, ProductCommentCreateReqVO.class);
        // mock 方法
        mockSpu(mockSku(reqVO.getSkuId()).getSpuId());

        // 调用
        productCommentService.createComment(reqVO);
        // 断言
        ProductCommentDO comment = productCommentMapper.selectOne(ProductCommentDO::getSkuId, 1L);
        assertEquals("芋艿", comment.getUserNickname());
        assertEquals(0L, comment.getUserId());
        assertNull(comment.getOrderItemId());
    }

    @Test
    public void testCreateComment_skuNotExists() {
        // 准备参数
        ProductCommentCreateReqVO reqVO = randomPojo(ProductCommentCreateReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> productCommentService.createComment(reqVO), SKU_NOT_EXISTS);
        assertEquals(0L, productCommentMapper.selectCount());
    }

    // ========== 会员评价订单项 ==========

    @Test
    public void testCreateCommentByMember_success() {
        // 准备参数
        ProductCommentCreateReqDTO reqDTO = randomPojo(ProductCommentCreateReqDTO.class, o -> o
                .setDescriptionScores(ProductCommentScoresEnum.THREE.getScores())
                .setBenefitScores(ProductCommentScoresEnum.FOUR.getScores()));
        // mock 数据：同一 SKU 已有虚拟评价，不影响会员评价
        productCommentMapper.insert(randomCommentDO(o -> o.setUserId(0L).setOrderId(null).setOrderItemId(null)
                .setSkuId(reqDTO.getSkuId())));
        // mock 方法
        ProductSkuDO sku = mockSku(reqDTO.getSkuId());
        ProductSpuDO spu = mockSpu(sku.getSpuId());
        MemberUserRespDTO user = randomPojo(MemberUserRespDTO.class, o -> o.setId(reqDTO.getUserId()));
        when(memberUserApi.getUser(eq(reqDTO.getUserId()))).thenReturn(user);

        // 调用
        Long commentId = productCommentService.createComment(reqDTO);
        // 断言
        ProductCommentDO comment = productCommentMapper.selectById(commentId);
        assertPojoEquals(reqDTO, comment);
        assertEquals(user.getNickname(), comment.getUserNickname());
        assertEquals(user.getAvatar(), comment.getUserAvatar());
        assertEquals(3, comment.getScores()); // (3 + 4) / 2 向下取整
        assertEquals(spu.getId(), comment.getSpuId());
        assertEquals(spu.getName(), comment.getSpuName());
        assertEquals(sku.getPicUrl(), comment.getSkuPicUrl());
        assertEquals(sku.getProperties(), comment.getSkuProperties());
    }

    @Test
    public void testCreateCommentByMember_orderExists() {
        // mock 数据：该会员已评价该订单项
        ProductCommentDO dbComment = randomCommentDO();
        productCommentMapper.insert(dbComment);
        // 准备参数
        ProductCommentCreateReqDTO reqDTO = randomPojo(ProductCommentCreateReqDTO.class, o -> o
                .setUserId(dbComment.getUserId()).setOrderItemId(dbComment.getOrderItemId()));
        // mock 方法
        mockSpu(mockSku(reqDTO.getSkuId()).getSpuId());

        // 调用，并断言异常
        assertServiceException(() -> productCommentService.createComment(reqDTO), COMMENT_ORDER_EXISTS);
        assertEquals(1L, productCommentMapper.selectCount());
        verify(memberUserApi, never()).getUser(any());
    }

    // ========== 查询评价 ==========

    @Test
    public void testGetComment_success() {
        // mock 数据
        ProductCommentDO dbComment = randomCommentDO();
        productCommentMapper.insert(dbComment);

        // 调用
        ProductCommentDO comment = productCommentService.getComment(dbComment.getId());
        // 断言
        assertPojoEquals(dbComment, comment);
    }

    @Test
    public void testGetComment_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> productCommentService.getComment(randomLongId()), COMMENT_NOT_EXISTS);
    }

    @Test
    public void testGetCommentPage_success() {
        // mock 数据
        ProductCommentDO dbComment = randomCommentDO(o -> { // 等会查询到
            o.setUserNickname("王二狗");
            o.setOrderId(1L);
            o.setSpuId(2L);
            o.setSpuName("感冒药");
            o.setScores(ProductCommentScoresEnum.FOUR.getScores());
            o.setReplyStatus(Boolean.TRUE);
            o.setCreateTime(buildTime(2023, 2, 1));
            o.setSkuProperties(null); // ProductSkuDO.Property 不可序列化，cloneIgnoreId 无法深拷贝
        });
        productCommentMapper.insert(dbComment);
        // 测试 userNickname 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setUserNickname("王三")));
        // 测试 orderId 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setOrderId(10L)));
        // 测试 spuId 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setSpuId(20L)));
        // 测试 spuName 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setSpuName("感康")));
        // 测试 scores 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setScores(ProductCommentScoresEnum.ONE.getScores())));
        // 测试 replyStatus 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setReplyStatus(Boolean.FALSE)));
        // 测试 createTime 不匹配
        productCommentMapper.insert(cloneIgnoreId(dbComment, o -> o.setCreateTime(buildTime(2023, 3, 1))));
        // 准备参数
        ProductCommentPageReqVO reqVO = new ProductCommentPageReqVO();
        reqVO.setUserNickname("王二");
        reqVO.setOrderId(1L);
        reqVO.setSpuId(2L);
        reqVO.setSpuName("感冒");
        reqVO.setScores(ProductCommentScoresEnum.FOUR.getScores());
        reqVO.setReplyStatus(Boolean.TRUE);
        reqVO.setCreateTime(buildBetweenTime(2023, 1, 1, 2023, 2, 28));

        // 调用
        PageResult<ProductCommentDO> pageResult = productCommentService.getCommentPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbComment, pageResult.getList().get(0));
    }

    @Test
    public void testGetAppCommentPage_success() {
        // mock 数据
        ProductCommentDO goodComment = randomCommentDO(o -> o.setSpuId(1L).setVisible(Boolean.TRUE)
                .setScores(ProductCommentScoresEnum.FIVE.getScores())
                .setSkuProperties(null)); // ProductSkuDO.Property 不可序列化，cloneIgnoreId 无法深拷贝
        productCommentMapper.insert(goodComment);
        ProductCommentDO mediocreComment = cloneIgnoreId(goodComment, o -> o.setScores(ProductCommentScoresEnum.THREE.getScores()));
        productCommentMapper.insert(mediocreComment);
        ProductCommentDO negativeComment = cloneIgnoreId(goodComment, o -> o.setScores(ProductCommentScoresEnum.ONE.getScores()));
        productCommentMapper.insert(negativeComment);
        // 测试 visible 不匹配
        productCommentMapper.insert(cloneIgnoreId(mediocreComment, o -> o.setVisible(Boolean.FALSE)));
        // 测试 spuId 不匹配
        productCommentMapper.insert(cloneIgnoreId(mediocreComment, o -> o.setSpuId(2L)));

        // 调用 + 断言：全部可见评价
        PageResult<ProductCommentDO> allResult = productCommentService.getCommentPage(
                new AppCommentPageReqVO().setSpuId(1L), Boolean.TRUE);
        assertEquals(3, allResult.getTotal());
        // 调用 + 断言：好评
        PageResult<ProductCommentDO> goodResult = productCommentService.getCommentPage(
                new AppCommentPageReqVO().setSpuId(1L).setType(AppCommentPageReqVO.GOOD_COMMENT), Boolean.TRUE);
        assertEquals(1, goodResult.getTotal());
        assertPojoEquals(goodComment, goodResult.getList().get(0));
        // 调用 + 断言：中评
        PageResult<ProductCommentDO> mediocreResult = productCommentService.getCommentPage(
                new AppCommentPageReqVO().setSpuId(1L).setType(AppCommentPageReqVO.MEDIOCRE_COMMENT), Boolean.TRUE);
        assertEquals(1, mediocreResult.getTotal());
        assertPojoEquals(mediocreComment, mediocreResult.getList().get(0));
        // 调用 + 断言：差评
        PageResult<ProductCommentDO> negativeResult = productCommentService.getCommentPage(
                new AppCommentPageReqVO().setSpuId(1L).setType(AppCommentPageReqVO.NEGATIVE_COMMENT), Boolean.TRUE);
        assertEquals(1, negativeResult.getTotal());
        assertPojoEquals(negativeComment, negativeResult.getList().get(0));
        // 调用 + 断言：不过滤可见性时，包含隐藏的中评
        PageResult<ProductCommentDO> mediocreAllResult = productCommentService.getCommentPage(
                new AppCommentPageReqVO().setSpuId(1L).setType(AppCommentPageReqVO.MEDIOCRE_COMMENT), null);
        assertEquals(2, mediocreAllResult.getTotal());
    }

    // ========== 更新评价 ==========

    @Test
    public void testUpdateCommentVisible_success() {
        // mock 数据
        ProductCommentDO dbComment = randomCommentDO(o -> o.setVisible(Boolean.TRUE));
        productCommentMapper.insert(dbComment);
        // 准备参数
        ProductCommentUpdateVisibleReqVO reqVO = new ProductCommentUpdateVisibleReqVO();
        reqVO.setId(dbComment.getId());
        reqVO.setVisible(Boolean.FALSE);

        // 调用
        productCommentService.updateCommentVisible(reqVO);
        // 断言
        ProductCommentDO comment = productCommentMapper.selectById(dbComment.getId());
        assertFalse(comment.getVisible());
        assertPojoEquals(dbComment, comment, "visible", "updater", "updateTime");
    }

    @Test
    public void testUpdateCommentVisible_notExists() {
        // 准备参数
        ProductCommentUpdateVisibleReqVO reqVO = new ProductCommentUpdateVisibleReqVO();
        reqVO.setId(randomLongId());
        reqVO.setVisible(Boolean.FALSE);

        // 调用，并断言异常
        assertServiceException(() -> productCommentService.updateCommentVisible(reqVO), COMMENT_NOT_EXISTS);
    }

    @Test
    public void testReplyComment_success() {
        // mock 数据
        ProductCommentDO dbComment = randomCommentDO(o -> o.setReplyStatus(Boolean.FALSE)
                .setReplyUserId(null).setReplyContent(null).setReplyTime(null));
        productCommentMapper.insert(dbComment);
        // 准备参数
        ProductCommentReplyReqVO reqVO = new ProductCommentReplyReqVO();
        reqVO.setId(dbComment.getId());
        reqVO.setReplyContent("测试");
        Long userId = randomLongId();

        // 调用
        productCommentService.replyComment(reqVO, userId);
        // 断言
        ProductCommentDO comment = productCommentMapper.selectById(dbComment.getId());
        assertTrue(comment.getReplyStatus());
        assertEquals(userId, comment.getReplyUserId());
        assertEquals("测试", comment.getReplyContent());
        assertNotNull(comment.getReplyTime());
    }

    @Test
    public void testReplyComment_notExists() {
        // 准备参数
        ProductCommentReplyReqVO reqVO = new ProductCommentReplyReqVO();
        reqVO.setId(randomLongId());
        reqVO.setReplyContent("测试");

        // 调用，并断言异常
        assertServiceException(() -> productCommentService.replyComment(reqVO, randomLongId()), COMMENT_NOT_EXISTS);
    }

    // ========== 随机对象 ==========

    private ProductSkuDO mockSku(Long skuId) {
        ProductSkuDO sku = randomPojo(ProductSkuDO.class, o -> o.setId(skuId)
                .setProperties(singletonList(new ProductSkuDO.Property(1L, "颜色", 2L, "红色"))));
        when(productSkuService.getSku(eq(skuId), eq(true))).thenReturn(sku);
        return sku;
    }

    private ProductSpuDO mockSpu(Long spuId) {
        ProductSpuDO spu = randomPojo(ProductSpuDO.class, o -> o.setId(spuId));
        when(productSpuService.getSpu(eq(spuId), eq(true))).thenReturn(spu);
        return spu;
    }

    @SafeVarargs
    private static ProductCommentDO randomCommentDO(Consumer<ProductCommentDO>... consumers) {
        Consumer<ProductCommentDO> consumer = (o) -> {
            o.setScores(randomEle(ProductCommentScoresEnum.values()).getScores()); // 保证 scores 的范围
            o.setDescriptionScores(randomEle(ProductCommentScoresEnum.values()).getScores());
            o.setBenefitScores(randomEle(ProductCommentScoresEnum.values()).getScores());
            o.setSkuProperties(singletonList(new ProductSkuDO.Property(1L, "颜色", 2L, "红色")));
        };
        return randomPojo(ProductCommentDO.class, ArrayUtils.append(consumer, consumers));
    }

}
