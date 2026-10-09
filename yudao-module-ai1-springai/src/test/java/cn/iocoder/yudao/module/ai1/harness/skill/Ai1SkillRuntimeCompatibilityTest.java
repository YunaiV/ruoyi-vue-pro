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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class Ai1SkillRuntimeCompatibilityTest {

    @TempDir
    private Path root;

    @Test
    public void testSkillsTool_duplicateName() throws Exception {
        // 准备参数
        Path first = Files.createDirectory(root.resolve("first"));
        Path second = Files.createDirectory(root.resolve("second"));
        Files.writeString(first.resolve("SKILL.md"), "---\nname: same\ndescription: first\n---\nfirst-content\n");
        Files.writeString(second.resolve("SKILL.md"), "---\nname: same\ndescription: second\n---\nsecond-content\n");
        ToolCallback callback = SkillsTool.builder().addSkillsDirectories(List.of(first.toString(), second.toString())).build();

        // 调用
        String result = callback.call("{\"command\":\"same\"}");
        // 断言
        assertTrue(result.contains("first-content"));
        assertTrue(result.contains(first.toString()));
        assertFalse(result.contains("second-content"));
        assertFalse(callback.getToolDefinition().description().contains("<description>second</description>"));
    }

    @Test
    public void testFileTools_pathConfinement() throws Exception {
        // 准备参数
        Path nested = Files.createDirectory(root.resolve("nested"));
        Files.writeString(nested.resolve("marker.txt"), "inside-marker");
        Path outside = Files.createTempDirectory(root.getParent(), "ai1-outside-");
        try {
            Files.writeString(outside.resolve("private.txt"), "outside-marker");
            Files.createSymbolicLink(root.resolve("escape"), outside);
            Map<String, ToolCallback> callbacks = callbacks();

            // 调用，并断言目录内搜索
            for (String path : List.of("nested", nested.toString())) {
                assertTrue(call(callbacks, "Glob", Map.of("pattern", "*.txt", "path", path)).contains("marker.txt"));
                assertTrue(call(callbacks, "Grep", Map.of("pattern", "inside-marker", "path", path)).contains("marker.txt"));
                assertTrue(call(callbacks, "ListDirectory", Map.of("path", path)).contains("marker.txt"));
            }
            // 调用，并断言目录外搜索
            for (String path : List.of("../" + outside.getFileName(), outside.toString(), "escape")) {
                assertFalse(call(callbacks, "Glob", Map.of("pattern", "*.txt", "path", path)).contains("private.txt"));
                assertFalse(call(callbacks, "Grep", Map.of("pattern", "outside-marker", "path", path)).contains("private.txt"));
                assertFalse(call(callbacks, "ListDirectory", Map.of("path", path)).contains("private.txt"));
            }
            // 调用，并断言目录外文件读写
            Path privateFile = outside.resolve("private.txt");
            assertFalse(call(callbacks, "Read", Map.of("filePath", privateFile.toString())).contains("outside-marker"));
            call(callbacks, "Write", Map.of("filePath", privateFile.toString(), "content", "changed"));
            call(callbacks, "Edit", Map.of("filePath", root.resolve("escape/private.txt").toString(),
                    "old_string", "outside-marker", "new_string", "changed"));
            // 断言
            assertEquals("outside-marker", Files.readString(privateFile));
        } finally {
            Files.deleteIfExists(root.resolve("escape"));
            Files.deleteIfExists(outside.resolve("private.txt"));
            Files.deleteIfExists(outside);
        }
    }

    @Test
    public void testTools_execute() throws Exception {
        // 准备参数
        Path skill = Files.createDirectory(root.resolve("probe"));
        Files.writeString(skill.resolve("SKILL.md"), "---\nname: probe\ndescription: 兼容性测试\n---\n执行测试脚本\n");
        Files.writeString(root.resolve("probe.txt"), "compatibility-ok\n");
        ToolCallback skillCallback = SkillsTool.builder().addSkillsDirectory(root.toString()).build();

        // 调用，并断言技能读取
        assertTrue(skillCallback.getToolDefinition().inputSchema().contains("command"));
        assertTrue(skillCallback.call("{\"command\":\"probe\"}").contains("执行测试脚本"));

        // 准备执行工具
        Map<String, ToolCallback> callbacks = Arrays.stream(ToolCallbacks.from(
                ShellTools.builder().workingDirectory(root).build(),
                FileSystemTools.builder().allowedDirectory(root).build(),
                GlobTool.builder().workingDirectory(root).allowedDirectory(root).build(),
                GrepTool.builder().workingDirectory(root).allowedDirectory(root).build(),
                ListDirectoryTool.builder().workingDirectory(root).allowedDirectory(root).build()))
                .collect(Collectors.toMap(callback -> callback.getToolDefinition().name(), Function.identity()));

        // 调用，并断言命令执行和文件搜索
        assertTrue(call(callbacks, "Bash", Map.of("command", "pwd", "timeout", 5000)).contains(root.toRealPath().toString()));
        assertTrue(call(callbacks, "Read", Map.of("filePath", root.resolve("probe.txt").toString())).contains("compatibility-ok"));
        assertTrue(call(callbacks, "Glob", Map.of("pattern", "*.txt")).contains("probe.txt"));
        assertTrue(call(callbacks, "Grep", Map.of("pattern", "compatibility-ok")).contains("probe.txt"));
        assertTrue(call(callbacks, "ListDirectory", Map.of()).contains("probe.txt"));

        // 调用
        call(callbacks, "Write", Map.of("filePath", root.resolve("written.txt").toString(), "content", "before"));
        // 断言
        assertEquals("before", Files.readString(root.resolve("written.txt")));
        // 调用
        call(callbacks, "Edit", Map.of("filePath", root.resolve("written.txt").toString(), "old_string", "before", "new_string", "after"));
        // 断言
        assertEquals("after", Files.readString(root.resolve("written.txt")));

        // 准备目录外文件
        Path outside = Files.createTempFile(root.getParent(), "ai1-outside-", ".txt");
        try {
            Files.writeString(outside, "private-marker");
            // 调用，并断言目录外访问
            assertFalse(call(callbacks, "Read", Map.of("filePath", outside.toString())).contains("private-marker"));
            assertFalse(call(callbacks, "Glob", Map.of("pattern", "*.txt", "path", root.getParent().toString())).contains(outside.getFileName().toString()));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    // ========== 工具方法 ==========

    private Map<String, ToolCallback> callbacks() {
        return Arrays.stream(ToolCallbacks.from(
                FileSystemTools.builder().allowedDirectory(root).build(),
                GlobTool.builder().workingDirectory(root).allowedDirectory(root).build(),
                GrepTool.builder().workingDirectory(root).allowedDirectory(root).build(),
                ListDirectoryTool.builder().workingDirectory(root).allowedDirectory(root).build()))
                .collect(Collectors.toMap(callback -> callback.getToolDefinition().name(), Function.identity()));
    }

    private static String call(Map<String, ToolCallback> callbacks, String name, Map<String, Object> arguments) {
        ToolCallback callback = callbacks.get(name);
        assertNotNull(callback, name);
        assertNotNull(JsonUtils.parseTree(callback.getToolDefinition().inputSchema()));
        return callback.call(JsonUtils.toJsonString(arguments));
    }
}
