package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.enums.image.AiFashionIntentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 服装设计 NLP 意图解析服务实现类
 *
 * <p>将用户的任意输入（一个字、短语、完整句子）解析为结构化设计意图。</p>
 *
 * @author deepay
 */
@Service
@Slf4j
public class AiFashionNlpParserServiceImpl implements AiFashionNlpParserService {

    /** 修改前缀词列表，按长度降序排列，保证最长匹配优先 */
    private static final List<String> MODIFY_PREFIXES = Arrays.asList(
            "换成", "改成", "改为", "变成", "改变", "调整", "换", "改", "变"
    );

    /** 批量关键词，如"款"/"件"/"个" */
    private static final Pattern ARABIC_BATCH_PATTERN = Pattern.compile("(\\d+)[款件个]");

    /** 批量颜色调色板（用于无指定颜色时的批量生成） */
    private static final String[][] BATCH_COLOR_PALETTE = {
            {"deep navy blue",       "#003153"},
            {"pure white",           "#FFFFFF"},
            {"classic black",        "#000000"},
            {"scarlet red",          "#FF2400"},
            {"sage green",           "#B2AC88"},
            {"dusty rose, feminine", "#DCAE96"},
            {"warm camel",           "#C19A6B"},
            {"lavender purple",      "#E6E6FA"},
            {"burnt orange",         "#CC5500"},
            {"powder blue",          "#B0E0E6"},
            {"champagne gold",       "#F7E7CE"},
            {"forest green, rich",   "#228B22"},
    };

    @Override
    public AiFashionIntentParseResult parse(String rawInput, AiFashionSessionContext sessionCtx) {
        if (StrUtil.isBlank(rawInput)) {
            return buildNewDesign(rawInput, sessionCtx, new ParsedEntities());
        }

        String input = rawInput.trim();

        // 1. 检测3D意图（最高优先级）
        for (String keyword : AiFashionNlpKeywords.THREE_D_KEYWORDS) {
            if (input.contains(keyword)) {
                return buildGenerate3D(input, sessionCtx);
            }
        }

        // 2. 检测批量数量
        int batchCount = detectBatchCount(input);

        // 3. 尝试剥离修改前缀
        boolean prefixStripped = false;
        String strippedInput = input;
        for (String prefix : MODIFY_PREFIXES) {
            if (input.startsWith(prefix)) {
                strippedInput = input.substring(prefix.length()).trim();
                prefixStripped = true;
                break;
            }
        }

        // 4. 实体检测（对剥离前缀后的输入）
        ParsedEntities entities = detectEntities(strippedInput);

        // 如果剥离前缀前也没有检测到，尝试对原始输入做检测（可能前缀不在开头）
        if (!entities.hasAnyModifier() && !prefixStripped) {
            entities = detectEntities(input);
        }

        boolean hasSession = sessionCtx != null && StrUtil.isNotBlank(sessionCtx.getLastPrompt());
        boolean onlyModifiers = entities.hasAnyModifier() && !entities.hasGarment();

        // 5. 意图决策
        AiFashionIntentEnum intent;
        if (batchCount > 1 && hasSession && onlyModifiers) {
            intent = AiFashionIntentEnum.BATCH_VARIANT;
        } else if (batchCount > 1) {
            intent = AiFashionIntentEnum.BATCH_GENERATE;
        } else if (onlyModifiers && hasSession && (input.length() <= 6 || prefixStripped)) {
            // 修改意图：决定具体修改类型
            intent = determineModifyIntent(entities);
        } else {
            intent = AiFashionIntentEnum.NEW_DESIGN;
        }

        // 6. 构建 prompt 和返回结果
        return buildResult(intent, batchCount, input, entities, sessionCtx, prefixStripped);
    }

    // ==============================
    // 私有辅助方法
    // ==============================

    /** 从输入中检测批量数量 */
    private int detectBatchCount(String input) {
        // 优先检测阿拉伯数字 + 款/件/个
        Matcher m = ARABIC_BATCH_PATTERN.matcher(input);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        // 按长度降序检测中文数量词（保证"一组"在"一"之前匹配）
        List<String> batchKeys = new ArrayList<>(AiFashionNlpKeywords.BATCH_NUMBER_MAP.keySet());
        batchKeys.sort((a, b) -> b.length() - a.length());
        for (String key : batchKeys) {
            if (input.contains(key)) {
                int count = AiFashionNlpKeywords.BATCH_NUMBER_MAP.get(key);
                if (count > 1) {
                    return count;
                }
            }
        }
        return 1;
    }

    /** 实体检测（最长匹配优先） */
    private ParsedEntities detectEntities(String input) {
        ParsedEntities e = new ParsedEntities();

        e.color       = longestMatchColor(input);
        e.style       = longestMatch(input, AiFashionNlpKeywords.STYLE_MAP);
        e.fabric      = longestMatch(input, AiFashionNlpKeywords.FABRIC_MAP);
        e.length      = longestMatch(input, AiFashionNlpKeywords.LENGTH_MAP);
        e.fit         = longestMatch(input, AiFashionNlpKeywords.FIT_MAP);
        e.garment     = longestMatch(input, AiFashionNlpKeywords.GARMENT_MAP);
        e.occasion    = longestMatch(input, AiFashionNlpKeywords.OCCASION_MAP);

        return e;
    }

    /** 通用最长匹配（Map<String, String>） */
    private String longestMatch(String input, Map<String, String> map) {
        List<String> keys = new ArrayList<>(map.keySet());
        keys.sort((a, b) -> b.length() - a.length());
        for (String key : keys) {
            if (input.contains(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    /** 颜色最长匹配（Map<String, String[]>，返回 [sdKeyword, hex]） */
    private String[] longestMatchColor(String input) {
        List<String> keys = new ArrayList<>(AiFashionNlpKeywords.COLOR_MAP.keySet());
        keys.sort((a, b) -> b.length() - a.length());
        for (String key : keys) {
            if (input.contains(key)) {
                return AiFashionNlpKeywords.COLOR_MAP.get(key);
            }
        }
        return null;
    }

    /** 确定具体修改意图 */
    private AiFashionIntentEnum determineModifyIntent(ParsedEntities entities) {
        if (entities.color != null) return AiFashionIntentEnum.MODIFY_COLOR;
        if (entities.style != null) return AiFashionIntentEnum.MODIFY_STYLE;
        if (entities.fabric != null) return AiFashionIntentEnum.MODIFY_FABRIC;
        if (entities.length != null) return AiFashionIntentEnum.MODIFY_LENGTH;
        if (entities.fit != null) return AiFashionIntentEnum.MODIFY_FIT;
        return AiFashionIntentEnum.MODIFY_CURRENT;
    }

    /** 构建 GENERATE_3D 结果 */
    private AiFashionIntentParseResult buildGenerate3D(String input, AiFashionSessionContext sessionCtx) {
        String basePrompt = sessionCtx != null && StrUtil.isNotBlank(sessionCtx.getLastPrompt())
                ? sessionCtx.getLastPrompt()
                : "fashion clothing, professional fashion photography, white background, full body shot";
        return AiFashionIntentParseResult.builder()
                .intent(AiFashionIntentEnum.GENERATE_3D)
                .batchCount(1)
                .basePrompt(basePrompt)
                .variantPrompts(List.of(basePrompt))
                .interpretation("已理解：生成当前设计的3D模型")
                .modifications(List.of())
                .workflowMode("STANDARD")
                .qualityPreset("HIGH")
                .build();
    }

    /** 构建新设计结果（无会话或无法识别为修改时的兜底） */
    private AiFashionIntentParseResult buildNewDesign(String input, AiFashionSessionContext sessionCtx, ParsedEntities entities) {
        String basePrompt = composePrompt(entities);
        return AiFashionIntentParseResult.builder()
                .intent(AiFashionIntentEnum.NEW_DESIGN)
                .batchCount(1)
                .basePrompt(basePrompt)
                .variantPrompts(List.of(basePrompt))
                .detectedColor(entities.color != null ? entities.color[0] : null)
                .detectedColorHex(entities.color != null ? entities.color[1] : null)
                .detectedStyle(entities.style)
                .detectedFabric(entities.fabric)
                .detectedLength(entities.length)
                .detectedFit(entities.fit)
                .detectedGarmentType(entities.garment)
                .interpretation("已理解：" + buildInterpretationText(AiFashionIntentEnum.NEW_DESIGN, 1, entities))
                .modifications(List.of())
                .workflowMode("STANDARD")
                .qualityPreset("HIGH")
                .build();
    }

    /** 核心 result 构建方法 */
    private AiFashionIntentParseResult buildResult(
            AiFashionIntentEnum intent,
            int batchCount,
            String rawInput,
            ParsedEntities entities,
            AiFashionSessionContext sessionCtx,
            boolean prefixStripped) {

        String basePrompt;
        List<String> variantPrompts;
        List<String> modifications = new ArrayList<>();

        // 修改类意图：从 session 中取 lastPrompt 并应用修改
        if (isModifyIntent(intent) && sessionCtx != null && StrUtil.isNotBlank(sessionCtx.getLastPrompt())) {
            basePrompt = applyModifications(sessionCtx.getLastPrompt(), entities, modifications);
        } else {
            basePrompt = composePrompt(entities);
        }

        // 构建变体 prompts
        if (batchCount <= 1) {
            variantPrompts = List.of(basePrompt);
        } else if (entities.color != null) {
            // 有颜色时按风格变化
            variantPrompts = buildStyleVariants(basePrompt, batchCount);
        } else {
            // 无颜色时按颜色变化
            variantPrompts = buildColorVariants(basePrompt, batchCount);
        }

        String workflowMode = suggestWorkflow(intent, batchCount);
        String qualityPreset = suggestQuality(intent, batchCount);
        String interpretation = "已理解：" + buildInterpretationText(intent, batchCount, entities);

        return AiFashionIntentParseResult.builder()
                .intent(intent)
                .batchCount(batchCount)
                .basePrompt(basePrompt)
                .variantPrompts(variantPrompts)
                .detectedColor(entities.color != null ? entities.color[0] : null)
                .detectedColorHex(entities.color != null ? entities.color[1] : null)
                .detectedStyle(entities.style)
                .detectedFabric(entities.fabric)
                .detectedLength(entities.length)
                .detectedFit(entities.fit)
                .detectedGarmentType(entities.garment)
                .interpretation(interpretation)
                .modifications(modifications)
                .workflowMode(workflowMode)
                .qualityPreset(qualityPreset)
                .build();
    }

    /** 对 lastPrompt 应用修改，填充 modifications 列表 */
    private String applyModifications(String lastPrompt, ParsedEntities entities, List<String> modifications) {
        String result = lastPrompt;
        if (entities.color != null) {
            modifications.add("颜色→" + entities.color[0]);
        }
        if (entities.fabric != null) {
            modifications.add("面料→" + entities.fabric);
        }
        if (entities.style != null) {
            modifications.add("风格→" + entities.style);
        }
        if (entities.length != null) {
            modifications.add("长度→" + entities.length);
        }
        if (entities.fit != null) {
            modifications.add("版型→" + entities.fit);
        }
        // 简单替换策略：将检测到的修改项追加/替换到 prompt
        if (entities.color != null) {
            result = replaceOrAppend(result, "color", entities.color[0]);
        }
        if (entities.fabric != null) {
            result = replaceOrAppend(result, "fabric", entities.fabric);
        }
        if (entities.style != null) {
            result = replaceOrAppend(result, "style", entities.style);
        }
        if (entities.length != null) {
            result = replaceOrAppend(result, "length", entities.length);
        }
        if (entities.fit != null) {
            result = replaceOrAppend(result, "fit", entities.fit);
        }
        return result;
    }

    /** 简单追加策略（实际项目中可做更精细的替换） */
    private String replaceOrAppend(String prompt, String category, String value) {
        // 尾部追加修改项
        return prompt + ", " + value;
    }

    /** 从实体组合 SD prompt */
    private String composePrompt(ParsedEntities e) {
        List<String> parts = new ArrayList<>();
        if (e.style != null)   parts.add(e.style);
        if (e.garment != null) parts.add(e.garment);
        else                   parts.add("fashion clothing");
        if (e.color != null)   parts.add(e.color[0]);
        if (e.length != null)  parts.add(e.length);
        if (e.fit != null)     parts.add(e.fit);
        if (e.fabric != null)  parts.add(e.fabric);
        if (e.occasion != null)parts.add(e.occasion);
        parts.add("professional fashion photography, white background, full body shot");
        return String.join(", ", parts);
    }

    /** 按风格变体构建批量 prompts */
    private List<String> buildStyleVariants(String basePrompt, int count) {
        String[] styleVariants = {"casual", "formal", "streetwear", "elegant", "minimalist", "avant-garde"};
        List<String> prompts = new ArrayList<>();
        for (int i = 0; i < Math.min(count, styleVariants.length); i++) {
            prompts.add(basePrompt + ", " + styleVariants[i] + " style");
        }
        // 如果需要更多，循环
        while (prompts.size() < count) {
            prompts.add(basePrompt + ", " + styleVariants[prompts.size() % styleVariants.length] + " style");
        }
        return prompts;
    }

    /** 按颜色变体构建批量 prompts */
    private List<String> buildColorVariants(String basePrompt, int count) {
        List<String> prompts = new ArrayList<>();
        int paletteSize = BATCH_COLOR_PALETTE.length;
        for (int i = 0; i < count; i++) {
            String[] colorEntry = BATCH_COLOR_PALETTE[i % paletteSize];
            prompts.add(basePrompt + ", " + colorEntry[0] + " color");
        }
        return prompts;
    }

    /** 构建用户可读的理解文本 */
    private String buildInterpretationText(AiFashionIntentEnum intent, int batchCount, ParsedEntities entities) {
        StringBuilder sb = new StringBuilder();
        switch (intent) {
            case BATCH_GENERATE:
            case BATCH_VARIANT:
                sb.append("批量生成").append(batchCount).append("款");
                if (entities.style != null) sb.append(entities.style);
                if (entities.garment != null) sb.append(entities.garment);
                else sb.append("服装");
                if (entities.color != null) sb.append("，颜色：").append(entities.color[0]);
                break;
            case MODIFY_COLOR:
                sb.append("将颜色改为").append(entities.color != null ? entities.color[0] : "新颜色")
                  .append("（保持其他设计不变）");
                break;
            case MODIFY_STYLE:
                sb.append("将风格改为").append(entities.style).append("（保持其他设计不变）");
                break;
            case MODIFY_FABRIC:
                sb.append("将面料改为").append(entities.fabric).append("（保持其他设计不变）");
                break;
            case MODIFY_LENGTH:
                sb.append("将长度改为").append(entities.length).append("（保持其他设计不变）");
                break;
            case MODIFY_FIT:
                sb.append("将版型改为").append(entities.fit).append("（保持其他设计不变）");
                break;
            case MODIFY_CURRENT:
                sb.append("综合修改当前设计");
                break;
            case GENERATE_3D:
                sb.append("生成3D模型");
                break;
            default:
                sb.append("生成");
                if (entities.style != null) sb.append(entities.style);
                if (entities.garment != null) sb.append(entities.garment);
                else sb.append("服装");
                if (entities.color != null) sb.append("，颜色：").append(entities.color[0]);
        }
        return sb.toString();
    }

    /** 推荐工作流模式 */
    private String suggestWorkflow(AiFashionIntentEnum intent, int batchCount) {
        if (intent == AiFashionIntentEnum.BATCH_GENERATE || intent == AiFashionIntentEnum.BATCH_VARIANT) {
            return "PROFESSIONAL";
        }
        if (isModifyIntent(intent)) {
            return "STANDARD";
        }
        return "STANDARD";
    }

    /** 推荐质量预设 */
    private String suggestQuality(AiFashionIntentEnum intent, int batchCount) {
        if (batchCount > 3) return "MEDIUM";
        if (isModifyIntent(intent)) return "HIGH";
        return "HIGH";
    }

    private boolean isModifyIntent(AiFashionIntentEnum intent) {
        return intent == AiFashionIntentEnum.MODIFY_COLOR
                || intent == AiFashionIntentEnum.MODIFY_STYLE
                || intent == AiFashionIntentEnum.MODIFY_FABRIC
                || intent == AiFashionIntentEnum.MODIFY_LENGTH
                || intent == AiFashionIntentEnum.MODIFY_FIT
                || intent == AiFashionIntentEnum.MODIFY_CURRENT;
    }

    /** 解析结果临时存储 */
    private static class ParsedEntities {
        String[] color;   // [sdKeyword, hex]
        String style;
        String fabric;
        String length;
        String fit;
        String garment;
        String occasion;

        boolean hasAnyModifier() {
            return color != null || style != null || fabric != null
                    || length != null || fit != null;
        }

        boolean hasGarment() {
            return garment != null;
        }
    }

}
