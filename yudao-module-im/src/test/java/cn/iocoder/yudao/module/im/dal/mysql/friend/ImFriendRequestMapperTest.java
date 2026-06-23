package cn.iocoder.yudao.module.im.dal.mysql.friend;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendRequestHandleResultEnum;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ImFriendRequestMapper} 的单元测试
 *
 * @author 芋道源码
 */
public class ImFriendRequestMapperTest extends BaseDbUnitTest {

    @Resource
    private ImFriendRequestMapper mapper;

    // ========== selectMyList ==========

    @Test
    public void testSelectMyList_bidirectional() {
        // 准备：1 既作为 from 又作为 to；3 是无关用户
        mapper.insert(buildRequest(1L, 2L));
        mapper.insert(buildRequest(2L, 1L));
        mapper.insert(buildRequest(3L, 4L));

        // 调用：cursor 为空，拉首页
        List<ImFriendRequestDO> list = mapper.selectMyList(1L, null, null, 10);

        // 断言：双向 OR 命中两条，无关用户被排除
        assertEquals(2, list.size());
        list.forEach(r -> assertTrue(r.getFromUserId().equals(1L) || r.getToUserId().equals(1L)));
    }

    @Test
    public void testSelectMyList_cursorPaging() {
        // 准备：三条 1 相关的申请
        ImFriendRequestDO r1 = buildRequest(1L, 2L);
        ImFriendRequestDO r2 = buildRequest(1L, 3L);
        ImFriendRequestDO r3 = buildRequest(1L, 4L);
        mapper.insert(r1);
        mapper.insert(r2);
        mapper.insert(r3);

        // 调用：cursor = r3，拉比 r3 更早的下一页
        ImFriendRequestDO cursor = mapper.selectById(r3.getId());
        List<ImFriendRequestDO> next = mapper.selectMyList(1L, cursor.getUpdateTime(), cursor.getId(), 10);

        // 断言：仅含 r1 / r2，按 id 倒序
        assertEquals(2, next.size());
        assertEquals(r2.getId(), next.get(0).getId());
        assertEquals(r1.getId(), next.get(1).getId());
    }

    @Test
    public void testSelectMyList_limit() {
        // 准备：插入 3 条
        for (int i = 0; i < 3; i++) {
            mapper.insert(buildRequest(1L, (long) (10 + i)));
        }

        // 调用：limit = 2
        List<ImFriendRequestDO> list = mapper.selectMyList(1L, null, null, 2);

        // 断言：手写 LIMIT 真生效
        assertEquals(2, list.size());
    }

    private static ImFriendRequestDO buildRequest(Long fromUserId, Long toUserId) {
        return new ImFriendRequestDO()
                .setFromUserId(fromUserId).setToUserId(toUserId)
                .setHandleResult(ImFriendRequestHandleResultEnum.UNHANDLED.getResult());
    }

}
