package cn.iocoder.yudao.module.infra.service.codegen.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodegenFormatUtilsTest {

    @Test
    void testNormalizeLineSeparators() {
        assertEquals("a\nb", CodegenFormatUtils.normalizeLineSeparators("a\r\nb"));
        assertEquals("a\nb", CodegenFormatUtils.normalizeLineSeparators("a\rb"));
    }

    @Test
    void testFormatGeneratedContent() {
        assertEquals("class A {\n}\n", CodegenFormatUtils.formatGeneratedContent("class A {\r\n}\r\n"));
        assertEquals("x\n", CodegenFormatUtils.formatGeneratedContent("x"));
        assertEquals("x\n", CodegenFormatUtils.formatGeneratedContent("x\n\n\n"));
        assertEquals("", CodegenFormatUtils.formatGeneratedContent(""));
        assertEquals("  \n", CodegenFormatUtils.formatGeneratedContent("  "));
    }

    @Test
    void testNormalizeForAssert() {
        assertEquals("}", CodegenFormatUtils.normalizeForAssert("}\n"));
        assertEquals("a\nb", CodegenFormatUtils.normalizeForAssert("a\r\nb\r\n"));
    }

    @Test
    void testNormalizeForAssert_matchesFormatGeneratedContentForAssert() {
        String raw = "package demo;\r\n\r\npublic class A {\r\n  private Long id;\r\n}\r\n";
        String generated = CodegenFormatUtils.formatGeneratedContent(raw);
        assertEquals(CodegenFormatUtils.normalizeForAssert(raw),
                CodegenFormatUtils.normalizeForAssert(generated));
    }

    @Test
    void testRemoveTrailingCommaBeforeCloseParen() {
        assertEquals("foo(bar\n)", CodegenFormatUtils.removeTrailingCommaBeforeCloseParen("foo(bar,\n)"));
    }

    @Test
    void testApplyVueTrailingCommaFix_classicVueReactive() {
        String input = "const queryParams = reactive({\n  createTime: [],\n})\n";
        String expected = "const queryParams = reactive({\n  createTime: []\n})\n";
        assertEquals(expected, CodegenFormatUtils.applyVueTrailingCommaFix(
                input, "codegen/vue3/views/index.vue.vm"));
    }

    @Test
    void testApplyVueTrailingCommaFix_skipVben5() {
        String input = "const x = {\n  a: 1,\n}\n";
        assertEquals(input, CodegenFormatUtils.applyVueTrailingCommaFix(
                input, "codegen/vue3_vben5_antd/general/views/index.vue.vm"));
    }

    @Test
    void testIsClassicVueTemplate() {
        assertTrue(CodegenFormatUtils.isClassicVueTemplate("codegen/vue/views/index.vue.vm"));
        assertTrue(CodegenFormatUtils.isClassicVueTemplate("codegen/vue3/views/index.vue.vm"));
        assertTrue(CodegenFormatUtils.isClassicVueTemplate("codegen/vue3/api/api.ts.vm"));
        assertFalse(CodegenFormatUtils.isClassicVueTemplate("codegen/vue3_vben5_antd/schema/views/index.vue.vm"));
        assertFalse(CodegenFormatUtils.isClassicVueTemplate("codegen/vue3_vben/views/index.vue.vm"));
        assertFalse(CodegenFormatUtils.isClassicVueTemplate("codegen/vue3_admin_uniapp/views/index.vue.vm"));
        assertFalse(CodegenFormatUtils.isClassicVueTemplate("codegen/vue30/views/index.vue.vm")); // 非 codegen/vue3/ 前缀
        assertFalse(CodegenFormatUtils.isClassicVueTemplate("codegen/java/controller/controller.vm"));
        assertFalse(CodegenFormatUtils.isClassicVueTemplate(null));
    }

    @Test
    void testNeedsReactiveTrailingCommaFix() {
        assertTrue(CodegenFormatUtils.needsReactiveTrailingCommaFix("codegen/vue3/views/index.vue.vm"));
        assertFalse(CodegenFormatUtils.needsReactiveTrailingCommaFix("codegen/vue3/api/api.ts.vm"));
        assertFalse(CodegenFormatUtils.needsReactiveTrailingCommaFix("codegen/vue3_vben5_antd/general/views/index.vue.vm"));
    }

    @Test
    void testNeedsVueTrailingCommaBeforeBraceFix() {
        assertTrue(CodegenFormatUtils.needsVueTrailingCommaBeforeBraceFix("codegen/vue3/views/index.vue.vm"));
        assertTrue(CodegenFormatUtils.needsVueTrailingCommaBeforeBraceFix("codegen/vue3_vben/views/index.vue.vm"));
        assertFalse(CodegenFormatUtils.needsVueTrailingCommaBeforeBraceFix("codegen/vue3_vben5_antd/general/views/index.vue.vm"));
        assertFalse(CodegenFormatUtils.needsVueTrailingCommaBeforeBraceFix("codegen/vue3_admin_uniapp/views/index.vue.vm"));
    }
}

