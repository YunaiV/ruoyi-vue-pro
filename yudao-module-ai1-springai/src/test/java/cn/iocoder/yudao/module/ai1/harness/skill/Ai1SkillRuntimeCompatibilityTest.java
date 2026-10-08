package cn.iocoder.yudao.module.ai1.harness.skill;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springaicommunity.agent.tools.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 AI1 使用的 agent-utils 工具的注册、JSON 入参和实际执行。
 */
public class Ai1SkillRuntimeCompatibilityTest {

    @TempDir
    private Path root;

    @Test
    public void testSkillAndExecutionTools() throws Exception {
        Path skill = Files.createDirectory(root.resolve("probe"));
        Files.writeString(skill.resolve("SKILL.md"), "---\nname: probe\ndescription: 兼容性测试\n---\n执行测试脚本\n");
        Files.writeString(root.resolve("probe.txt"), "compatibility-ok\n");
        ToolCallback skillCallback = SkillsTool.builder().addSkillsDirectory(root.toString()).build();
        assertTrue(skillCallback.getToolDefinition().inputSchema().contains("command"));
        assertTrue(skillCallback.call("{\"command\":\"probe\"}").contains("执行测试脚本"));

        Map<String, ToolCallback> callbacks = Arrays.stream(ToolCallbacks.from(
                ShellTools.builder().workingDirectory(root).build(),
                FileSystemTools.builder().allowedDirectory(root).build(),
                GlobTool.builder().workingDirectory(root).allowedDirectory(root).build(),
                GrepTool.builder().workingDirectory(root).allowedDirectory(root).build(),
                ListDirectoryTool.builder().workingDirectory(root).allowedDirectory(root).build()))
                .collect(Collectors.toMap(callback -> callback.getToolDefinition().name(), Function.identity()));

        assertTrue(call(callbacks, "Bash", Map.of("command", "pwd", "timeout", 5000)).contains(root.toRealPath().toString()));
        assertTrue(call(callbacks, "Read", Map.of("filePath", root.resolve("probe.txt").toString())).contains("compatibility-ok"));
        assertTrue(call(callbacks, "Glob", Map.of("pattern", "*.txt")).contains("probe.txt"));
        assertTrue(call(callbacks, "Grep", Map.of("pattern", "compatibility-ok")).contains("probe.txt"));
        assertTrue(call(callbacks, "ListDirectory", Map.of()).contains("probe.txt"));

        call(callbacks, "Write", Map.of("filePath", root.resolve("written.txt").toString(), "content", "before"));
        assertEquals("before", Files.readString(root.resolve("written.txt")));
        call(callbacks, "Edit", Map.of("filePath", root.resolve("written.txt").toString(), "old_string", "before", "new_string", "after"));
        assertEquals("after", Files.readString(root.resolve("written.txt")));

        // 保留 0.12.0 的目录约束，不能因迁移而放宽文件和搜索工具访问范围。
        Path outside = Files.createTempFile(root.getParent(), "ai1-outside-", ".txt");
        try {
            Files.writeString(outside, "private-marker");
            assertFalse(call(callbacks, "Read", Map.of("filePath", outside.toString())).contains("private-marker"));
            assertFalse(call(callbacks, "Glob", Map.of("pattern", "*.txt", "path", root.getParent().toString())).contains(outside.getFileName().toString()));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    private static String call(Map<String, ToolCallback> callbacks, String name, Map<String, Object> arguments) {
        ToolCallback callback = callbacks.get(name);
        assertNotNull(callback, name);
        assertNotNull(JsonUtils.parseTree(callback.getToolDefinition().inputSchema()));
        return callback.call(JsonUtils.toJsonString(arguments));
    }
}
