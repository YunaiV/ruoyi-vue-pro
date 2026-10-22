package cn.iocoder.yudao.module.ai1.service.memory;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.memory.vo.Ai1MemoryUpdateReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.agent.Ai1AgentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.memory.Ai1MemoryDO;
import cn.iocoder.yudao.module.ai1.dal.mysql.memory.Ai1MemoryMapper;
import cn.iocoder.yudao.module.ai1.service.agent.Ai1AgentService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.MEMORY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link Ai1MemoryServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(Ai1MemoryServiceImpl.class)
public class Ai1MemoryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private Ai1MemoryServiceImpl memoryService;

    @Resource
    private Ai1MemoryMapper memoryMapper;

    @MockitoBean
    private Ai1AgentService agentService;

    @Test
    public void testUpdateMemory_success() {
        // mock 数据
        Ai1MemoryDO memory = insertMemory(1L, 100L);

        // 调用
        memoryService.updateMemory(new Ai1MemoryUpdateReqVO().setId(memory.getId()).setContent("## 用户画像\n- 新内容"));
        // 断言：正文更新，水位线不变
        Ai1MemoryDO result = memoryMapper.selectById(memory.getId());
        assertEquals("## 用户画像\n- 新内容", result.getContent());
        assertEquals(10L, result.getLastMessageId());
    }

    @Test
    public void testUpdateMemory_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> memoryService.updateMemory(new Ai1MemoryUpdateReqVO().setId(1L).setContent("内容")),
                MEMORY_NOT_EXISTS);
    }

    @Test
    public void testDeleteMemoryListByIds_success() {
        // mock 数据
        Ai1MemoryDO memory1 = insertMemory(1L, 100L);
        Ai1MemoryDO memory2 = insertMemory(1L, 200L);
        Ai1MemoryDO memory3 = insertMemory(2L, 100L);

        // 调用
        memoryService.deleteMemoryListByIds(Arrays.asList(memory1.getId(), memory2.getId()));
        // 断言：逻辑删除后不再可见，同一用户可以重新生成
        assertNull(memoryMapper.selectById(memory1.getId()));
        assertNull(memoryMapper.selectById(memory2.getId()));
        assertNotNull(memoryMapper.selectById(memory3.getId()));
        assertNotNull(insertMemory(1L, 100L).getId());
    }

    @Test
    public void testDeleteMemoryListByIds_notExists() {
        // mock 数据
        Ai1MemoryDO memory = insertMemory(1L, 100L);

        // 调用，并断言异常
        assertServiceException(() -> memoryService.deleteMemoryListByIds(Arrays.asList(memory.getId(), memory.getId() + 1)),
                MEMORY_NOT_EXISTS);
        // 断言
        assertNotNull(memoryMapper.selectById(memory.getId()));
    }

    @Test
    public void testDeleteMemoryListByAgentIds() {
        // mock 数据
        Ai1MemoryDO memory1 = insertMemory(1L, 100L);
        Ai1MemoryDO memory2 = insertMemory(2L, 100L);
        Ai1MemoryDO memory3 = insertMemory(3L, 100L);

        // 调用
        memoryService.deleteMemoryListByAgentIds(Arrays.asList(1L, 2L));
        memoryService.deleteMemoryListByAgentIds(Collections.emptyList());
        // 断言
        assertNull(memoryMapper.selectById(memory1.getId()));
        assertNull(memoryMapper.selectById(memory2.getId()));
        assertNotNull(memoryMapper.selectById(memory3.getId()));
    }

    @Test
    public void testGetMemoryPage() {
        // mock 数据
        Ai1MemoryDO memory1 = insertMemory(1L, 100L);
        Ai1MemoryDO memory2 = insertMemory(1L, 200L);
        Ai1MemoryDO memory3 = insertMemory(2L, 100L);
        memoryService.deleteMemory(insertMemory(1L, 300L).getId());

        // 调用：不传条件，按编号倒序，不含已删除的记忆
        PageResult<Ai1MemoryDO> pageResult = memoryService.getMemoryPage(new Ai1MemoryPageReqVO());
        // 断言
        assertEquals(List.of(memory3.getId(), memory2.getId(), memory1.getId()),
                pageResult.getList().stream().map(Ai1MemoryDO::getId).toList());

        // 调用：按 Agent、用户筛选
        pageResult = memoryService.getMemoryPage(new Ai1MemoryPageReqVO().setAgentId(1L).setUserId(100L));
        // 断言
        assertEquals(List.of(memory1.getId()), pageResult.getList().stream().map(Ai1MemoryDO::getId).toList());
    }

    @Test
    public void testGetMemoryByAgentIdAndUserId() {
        // mock 数据
        Ai1MemoryDO memory = insertMemory(1L, 100L);
        insertMemory(1L, 200L);

        // 调用，并断言
        assertEquals(memory.getId(), memoryService.getMemoryByAgentIdAndUserId(1L, 100L).getId());
        assertNull(memoryService.getMemoryByAgentIdAndUserId(2L, 100L));
    }

    @Test
    public void testGetMemoryByAgentIdAndUserId_duplicate() {
        // mock 数据：极端并发下同一用户产生了重复记录
        insertMemory(1L, 100L);
        insertMemory(1L, 100L);

        // 调用，并断言：不报错，返回其中一条
        assertNotNull(memoryService.getMemoryByAgentIdAndUserId(1L, 100L));
    }

    @Test
    public void testCreateMemoryIfAgentExists() {
        // mock 方法：Agent 1 存在，Agent 2 已删除
        when(agentService.getAgent(1L)).thenReturn(Ai1AgentDO.builder().id(1L).name("客服")
                .status(CommonStatusEnum.ENABLE.getStatus()).build());

        // 调用，并断言：Agent 已删除时不创建
        assertFalse(memoryService.createMemoryIfAgentExists(Ai1MemoryDO.builder().agentId(2L)
                .userId(100L).content("迟到的归档结果").lastMessageId(20L).build()));
        assertNull(memoryMapper.selectByAgentIdAndUserId(2L, 100L));
        // 调用，并断言：Agent 存在时创建
        assertTrue(memoryService.createMemoryIfAgentExists(Ai1MemoryDO.builder().agentId(1L)
                .userId(100L).content("用户偏好").lastMessageId(20L).build()));
        assertEquals("用户偏好", memoryMapper.selectByAgentIdAndUserId(1L, 100L).getContent());
        // 调用，并断言：已有未删除的记忆时不重复创建
        assertFalse(memoryService.createMemoryIfAgentExists(Ai1MemoryDO.builder().agentId(1L)
                .userId(100L).content("重复归档").lastMessageId(30L).build()));
        assertEquals(1, memoryMapper.selectCount(Ai1MemoryDO::getAgentId, 1L));
    }

    @Test
    public void testCreateMemoryIfAgentExists_afterDelete() {
        // mock 数据：已删除的历史记忆
        when(agentService.getAgent(1L)).thenReturn(Ai1AgentDO.builder().id(1L).name("客服")
                .status(CommonStatusEnum.ENABLE.getStatus()).build());
        Ai1MemoryDO old = insertMemory(1L, 100L);
        memoryService.deleteMemory(old.getId());

        // 调用：删除后按历史消息重新生成
        assertTrue(memoryService.createMemoryIfAgentExists(Ai1MemoryDO.builder().agentId(1L)
                .userId(100L).content("重建记忆").lastMessageId(5L).build()));
        // 断言：生成新的记录与水位线，历史记录不影响按用户查询
        Ai1MemoryDO memory = memoryService.getMemoryByAgentIdAndUserId(1L, 100L);
        assertNotEquals(old.getId(), memory.getId());
        assertEquals("重建记忆", memory.getContent());
        assertEquals(5L, memory.getLastMessageId());
        // 断言：旧快照的归档写入不能复活已删除的记录
        assertFalse(memoryService.updateMemoryIfUnchanged(old, "迟到的归档结果", 30L));
        assertNull(memoryService.getMemory(old.getId()));
    }

    @Test
    public void testUpdateMemoryIfUnchanged() {
        // mock 数据
        Ai1MemoryDO original = insertMemory(1L, 100L);

        // 调用，并断言：首次写入成功，按旧快照再次写入失败
        assertTrue(memoryService.updateMemoryIfUnchanged(original, "新记忆", 20L));
        assertFalse(memoryService.updateMemoryIfUnchanged(original, "过期结果", 30L));
        Ai1MemoryDO memory = memoryMapper.selectById(original.getId());
        assertEquals("新记忆", memory.getContent());
        assertEquals(20L, memory.getLastMessageId());
    }

    @Test
    public void testUpdateMemoryIfUnchanged_edited() {
        // mock 数据：归档读取后，人工只在末尾加了空格，水位线不变
        Ai1MemoryDO original = insertMemory(1L, 100L);
        memoryMapper.updateById(new Ai1MemoryDO().setId(original.getId()).setContent("## 用户画像\n- 旧内容 "));

        // 调用，并断言：放弃写入，保留人工修改
        assertFalse(memoryService.updateMemoryIfUnchanged(original, "归档结果", 20L));
        Ai1MemoryDO memory = memoryMapper.selectById(original.getId());
        assertEquals("## 用户画像\n- 旧内容 ", memory.getContent());
        assertEquals(10L, memory.getLastMessageId());
    }

    // ========== 测试数据 ==========

    private Ai1MemoryDO insertMemory(Long agentId, Long userId) {
        Ai1MemoryDO memory = Ai1MemoryDO.builder().agentId(agentId).userId(userId)
                .content("## 用户画像\n- 旧内容").lastMessageId(10L).build();
        memoryMapper.insert(memory);
        return memory;
    }

}
