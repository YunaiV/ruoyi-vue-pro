package cn.iocoder.yudao.module.im.dal.mysql.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(statements = "DELETE FROM im_channel_message", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImChannelMessageMapperTest extends BaseDbUnitTest {

    @Resource
    private ImChannelMessageMapper mapper;

    @Test
    public void testSelectListByUserAndMinId_includesBroadcastAndMatchingRecipients() {
        ImChannelMessageDO nullRecipients = insertMessage(null);
        ImChannelMessageDO emptyRecipients = insertMessage(List.of());
        ImChannelMessageDO matchingRecipients = insertMessage(List.of(1L, 2L));
        insertMessage(List.of(2L, 11L)); // 用户 1 不应误命中用户 11
        ImChannelMessageDO deleted = insertMessage(List.of(1L));
        mapper.deleteById(deleted.getId());

        List<ImChannelMessageDO> result = mapper.selectListByUserAndMinId(1L, 0L, 100);

        assertEquals(List.of(nullRecipients.getId(), emptyRecipients.getId(), matchingRecipients.getId()),
                result.stream().map(ImChannelMessageDO::getId).toList());
    }

    @Test
    public void testSelectListByUserAndMinId_respectsCursorAndLimit() {
        ImChannelMessageDO cursor = insertMessage(null);
        ImChannelMessageDO next = insertMessage(List.of());
        insertMessage(List.of(1L));

        List<ImChannelMessageDO> result = mapper.selectListByUserAndMinId(1L, cursor.getId(), 1);

        assertEquals(List.of(next.getId()), result.stream().map(ImChannelMessageDO::getId).toList());
    }

    private ImChannelMessageDO insertMessage(List<Long> receiverUserIds) {
        ImChannelMessageDO message = ImChannelMessageDO.builder()
                .channelId(10L)
                .materialId(20L)
                .type(1)
                .content("{}")
                .receiverUserIds(receiverUserIds)
                .sendTime(LocalDateTime.now())
                .build();
        mapper.insert(message);
        return message;
    }

}
