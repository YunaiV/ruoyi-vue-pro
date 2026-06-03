package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.util.StrUtil;

/**
 * 代码生成结果格式化工具
 */
final class CodegenFormatUtils {

    /**
     * 经典 Vue2 模板目录，与 {@link CodegenEngine#vueTemplatePath(String)} 一致
     */
    private static final String CLASSIC_VUE2_TEMPLATE_PREFIX = "codegen/vue/";

    /**
     * 经典 Vue3 模板目录，与 {@link CodegenEngine#vue3TemplatePath(String)} 一致。
     * 使用前缀 {@code codegen/vue3/} 而非子串匹配，避免误伤 {@code vue3_vben5}、{@code vue3_admin_uniapp} 等目录。
     */
    private static final String CLASSIC_VUE3_TEMPLATE_PREFIX = "codegen/vue3/";

    private CodegenFormatUtils() {
    }

    /**
     * 经典 Vue2 / Vue3 模板（Element UI / Element Plus 标准模版）
     */
    static boolean isClassicVueTemplate(String vmPath) {
        if (StrUtil.isEmpty(vmPath)) {
            return false;
        }
        return StrUtil.startWith(vmPath, CLASSIC_VUE2_TEMPLATE_PREFIX)
                || StrUtil.startWith(vmPath, CLASSIC_VUE3_TEMPLATE_PREFIX);
    }

    /**
     * 是否处理 {@code ,\n}} / {@code ,\n  }} 形式的末尾逗号（经典 Vue + vue3_vben2；排除 vben5、uniapp）
     */
    static boolean needsVueTrailingCommaBeforeBraceFix(String vmPath) {
        if (StrUtil.containsAny(vmPath, "vben5", "vue3_admin_uniapp")) {
            return false;
        }
        return isClassicVueTemplate(vmPath) || StrUtil.startWith(vmPath, "codegen/vue3_vben/");
    }

    /**
     * 是否处理 {@code ,\n)} 形式的末尾逗号（仅经典 Vue 的 .vue 视图模板）
     */
    static boolean needsReactiveTrailingCommaFix(String vmPath) {
        return isClassicVueTemplate(vmPath) && StrUtil.endWith(vmPath, ".vue.vm");
    }

    /**
     * Vue 模板：统一去除对象/参数列表末尾多余逗号（与 {@link CodegenEngine#prettyCode} 历史行为一致）
     */
    static String applyVueTrailingCommaFix(String content, String vmPath) {
        if (content == null) {
            return null;
        }
        content = normalizeLineSeparators(content);
        if (needsVueTrailingCommaBeforeBraceFix(vmPath)) {
            content = content.replaceAll(",\\r?\\n}", "\n}")
                    .replaceAll(",\\r?\\n  }", "\n  }");
        }
        if (needsReactiveTrailingCommaFix(vmPath)) {
            content = removeTrailingCommaBeforeCloseParen(content);
        }
        return content;
    }

    /**
     * 去除多行实参中 {@code ,\n)} 形式的末尾逗号（如 {@code foo(bar,\n)}；{@code reactive} 的 {@code ,\n})} 由 {@code ,\n}} 规则处理）
     */
    static String removeTrailingCommaBeforeCloseParen(String content) {
        if (content == null) {
            return null;
        }
        return content.replaceAll(",\\r?\\n\\)", "\n)");
    }

    /**
     * 统一换行符为 LF
     */
    static String normalizeLineSeparators(String content) {
        if (content == null) {
            return null;
        }
        return content.replace("\r\n", "\n").replace('\r', '\n');
    }

    /**
     * 生产生成结果：LF + 保证文件以单个换行结尾（符合 POSIX / IDE 习惯）
     */
    static String formatGeneratedContent(String content) {
        content = normalizeLineSeparators(content);
        if (StrUtil.isEmpty(content)) {
            return content;
        }
        while (content.endsWith("\n\n")) {
            content = content.substring(0, content.length() - 1);
        }
        if (!content.endsWith("\n")) {
            content = content + "\n";
        }
        return content;
    }

    /**
     * 单测断言：LF + 去掉末尾空白（与历史 fixture 无尾换行对齐）
     */
    static String normalizeForAssert(String content) {
        if (content == null) {
            return null;
        }
        return normalizeLineSeparators(content).stripTrailing();
    }
}
