package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ImChannelMessageMapper} 的单元测试
 *
 * @author 芋道源码
 */
public class ImChannelMessageMapperTest extends BaseDbUnitTest {

    @Resource
    private ImChannelMessageMapper channelMessageMapper;

    @Test
    public void testSelectListByUserAndMinId_broadcastAndMatched() {
        // mock 数据：全员（null）
        ImChannelMessageDO nullReceiverMessage = createChannelMessage(null);
        // mock 数据：全员（空列表）
        ImChannelMessageDO emptyReceiverMessage = createChannelMessage(Collections.emptyList());
        // mock 数据：定向，包含用户 1
        ImChannelMessageDO matchedMessage = createChannelMessage(Arrays.asList(1L, 2L));
        // mock 数据：定向，不包含用户 1（用户 1 不应误命中用户 11）
        createChannelMessage(Arrays.asList(2L, 11L));
        // mock 数据：已删除
        ImChannelMessageDO deletedMessage = createChannelMessage(Collections.singletonList(1L));
        channelMessageMapper.deleteById(deletedMessage.getId());

        // 调用
        List<ImChannelMessageDO> list = channelMessageMapper.selectListByUserAndMinId(1L, 0L, 100);
        // 断言
        assertEquals(Arrays.asList(nullReceiverMessage.getId(), emptyReceiverMessage.getId(), matchedMessage.getId()),
                convertList(list, ImChannelMessageDO::getId));
    }

    @Test
    public void testSelectListByUserAndMinId_cursorAndLimit() {
        // mock 数据
        ImChannelMessageDO cursorMessage = createChannelMessage(null);
        ImChannelMessageDO nextMessage = createChannelMessage(Collections.emptyList());
        createChannelMessage(Collections.singletonList(1L));

        // 调用：从游标之后拉取 1 条
        List<ImChannelMessageDO> list = channelMessageMapper.selectListByUserAndMinId(1L, cursorMessage.getId(), 1);
        // 断言
        assertEquals(Collections.singletonList(nextMessage.getId()), convertList(list, ImChannelMessageDO::getId));
    }

    private ImChannelMessageDO createChannelMessage(List<Long> receiverUserIds) {
        ImChannelMessageDO message = ImChannelMessageDO.builder()
                .channelId(10L)
                .materialId(20L)
                .type(1)
                .content("{}")
                .receiverUserIds(receiverUserIds)
                .sendTime(LocalDateTime.now())
                .build();
        channelMessageMapper.insert(message);
        return message;
    }

}
