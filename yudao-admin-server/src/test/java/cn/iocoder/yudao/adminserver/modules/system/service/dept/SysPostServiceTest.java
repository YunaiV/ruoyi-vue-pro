package cn.iocoder.yudao.adminserver.modules.system.service.dept;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostUpdateReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysPostDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dept.SysPostMapper;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.impl.SysPostServiceImpl;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SysPostServiceImpl} 的单元测试类
 *
 * @author niudehua
 */
@Import(SysPostServiceImpl.class)
class SysPostServiceTest extends BaseDbUnitTest {

    @Resource
    private SysPostServiceImpl postService;
    @Resource
    private SysPostMapper postMapper;

    @Test
    void testPagePosts() {
        // mock 数据
        SysPostDO postDO = randomPojo(SysPostDO.class, o -> {
            o.setName("码仔");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        postMapper.insert(postDO);
        // 测试 name 不匹配
        postMapper.insert(ObjectUtils.cloneIgnoreId(postDO, o -> o.setName("程序员")));
        // 测试 status 不匹配
        postMapper.insert(ObjectUtils.cloneIgnoreId(postDO, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 准备参数
        SysPostPageReqVO reqVO = new SysPostPageReqVO();
        reqVO.setName("码");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<SysPostDO> pageResult = postService.getPostPage(reqVO);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(postDO, pageResult.getList().get(0));
    }

    @Test
    void testListPosts() {
        // mock 数据
        SysPostDO postDO = randomPojo(SysPostDO.class, o -> {
            o.setName("码仔");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        postMapper.insert(postDO);
        // 测试 name 不匹配
        postMapper.insert(ObjectUtils.cloneIgnoreId(postDO, o -> o.setName("程序员")));
        // 测试 status 不匹配
        postMapper.insert(ObjectUtils.cloneIgnoreId(postDO, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        SysPostExportReqVO reqVO = new SysPostExportReqVO();
        reqVO.setName("码");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<SysPostDO> list = postService.getPosts(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(postDO, list.get(0));
    }

    @Test
    void testGetPost() {
        // mock 数据
        SysPostDO dbPostDO = randomPostDO();
        postMapper.insert(dbPostDO);
        // 准备参数
        Long id = dbPostDO.getId();
        // 调用
        SysPostDO post = postService.getPost(id);
        // 断言
        assertNotNull(post);
        assertPojoEquals(dbPostDO, post);
    }

    @Test
    void testCreatePost_success() {
        // 准备参数
        SysPostCreateReqVO reqVO = randomPojo(SysPostCreateReqVO.class,
            o -> o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()));
        // 调用
        Long postId = postService.createPost(reqVO);
        // 断言
        assertNotNull(postId);
        // 校验记录的属性是否正确
        SysPostDO post = postMapper.selectById(postId);
        assertPojoEquals(reqVO, post);
    }

    @Test
    void testUpdatePost_success() {
        // mock 数据
        SysPostDO postDO = randomPostDO();
        postMapper.insert(postDO);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysPostUpdateReqVO reqVO = randomPojo(SysPostUpdateReqVO.class,
            o -> {
                // 设置更新的 ID
                o.setId(postDO.getId());
                o.setStatus(randomEle(CommonStatusEnum.values()).getStatus());
            });
        // 调用
        postService.updatePost(reqVO);
        // 校验是否更新正确
        SysPostDO post = postMapper.selectById(reqVO.getId());// 获取最新的
        assertPojoEquals(reqVO, post);
    }

    @Test
    void testDeletePost_success() {
        // mock 数据
        SysPostDO postDO = randomPostDO();
        postMapper.insert(postDO);
        // 准备参数
        Long id = postDO.getId();
        // 调用
        postService.deletePost(id);
        assertNull(postMapper.selectById(id));
    }

    @Test
    void testCheckPost_notFoundForDelete() {
        // 准备参数
        Long id = randomLongId();
        // 调用, 并断言异常
        assertServiceException(() -> postService.deletePost(id), POST_NOT_FOUND);
    }

    @Test
    void testCheckPost_nameDuplicateForCreate() {
        // mock 数据
        SysPostDO postDO = randomPostDO();
        postMapper.insert(postDO);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysPostCreateReqVO reqVO = randomPojo(SysPostCreateReqVO.class,
            // 模拟 name 重复
            o -> o.setName(postDO.getName()));
        assertServiceException(() -> postService.createPost(reqVO), POST_NAME_DUPLICATE);
    }

    @Test
    void testCheckPost_codeDuplicateForUpdate() {
        // mock 数据
        SysPostDO postDO = randomPostDO();
        postMapper.insert(postDO);
        // mock 数据 稍后模拟重复它的 code
        SysPostDO codePostDO = randomPostDO();
        postMapper.insert(codePostDO);
        // 准备参数
        SysPostUpdateReqVO reqVO = randomPojo(SysPostUpdateReqVO.class,
            o -> {
                // 设置更新的 ID
                o.setId(postDO.getId());
                // 模拟 code 重复
                o.setCode(codePostDO.getCode());
            });
        // 调用, 并断言异常
        assertServiceException(() -> postService.updatePost(reqVO), POST_CODE_DUPLICATE);
    }

    @SafeVarargs
    private static SysPostDO randomPostDO(Consumer<SysPostDO>... consumers) {
        Consumer<SysPostDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(SysPostDO.class, ArrayUtils.append(consumer, consumers));
    }
}
