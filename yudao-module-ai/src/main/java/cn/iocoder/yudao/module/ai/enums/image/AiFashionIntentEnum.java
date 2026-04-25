package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计智能交互意图枚举
 *
 * <p>由 {@code AiFashionNlpParserService} 从用户输入中解析出意图类型。</p>
 *
 * <pre>
 * 单字/短语意图（修改类）：
 *   "红"       → MODIFY_COLOR
 *   "改短"     → MODIFY_LENGTH
 *   "牛仔"     → MODIFY_FABRIC
 *   "甜酷风"   → MODIFY_STYLE
 *   "宽松"     → MODIFY_FIT
 *
 * 句子意图（新建类）：
 *   "出一款连衣裙"     → NEW_DESIGN
 *   "来5款不同颜色"    → BATCH_GENERATE
 *   "换个颜色出3款"    → BATCH_VARIANT
 *
 * 3D 意图：
 *   "生成3D" / "转3D" → GENERATE_3D
 * </pre>
 *
 * @author deepay
 */
@AllArgsConstructor
@Getter
public enum AiFashionIntentEnum {

    NEW_DESIGN("NEW_DESIGN", "全新设计"),
    BATCH_GENERATE("BATCH_GENERATE", "批量生成多款"),
    BATCH_VARIANT("BATCH_VARIANT", "基于当前批量变体"),
    MODIFY_COLOR("MODIFY_COLOR", "修改颜色"),
    MODIFY_STYLE("MODIFY_STYLE", "修改风格/款式"),
    MODIFY_FABRIC("MODIFY_FABRIC", "修改面料"),
    MODIFY_LENGTH("MODIFY_LENGTH", "修改长度"),
    MODIFY_FIT("MODIFY_FIT", "修改版型"),
    MODIFY_CURRENT("MODIFY_CURRENT", "综合修改当前设计"),
    GENERATE_3D("GENERATE_3D", "生成3D模型");

    private final String code;
    private final String desc;

}
