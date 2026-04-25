package cn.iocoder.yudao.module.ai.service.image;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 服装设计中文词汇表
 *
 * <p>将中文服装设计词汇映射为 Stable Diffusion 提示词关键字，
 * 是 {@link AiFashionNlpParserServiceImpl} 的核心数据源。</p>
 *
 * <p>格式：{@code Map<中文词, SD关键词>}，每个中文词有对应的颜色 Hex（颜色表专用）。</p>
 *
 * @author deepay
 */
public final class AiFashionNlpKeywords {

    private AiFashionNlpKeywords() {}

    // ========== 颜色词汇 ==========
    // Map<中文词, [SD关键词, Hex]>
    public static final Map<String, String[]> COLOR_MAP = new LinkedHashMap<>();
    static {
        // 红系
        put(COLOR_MAP, "红",    "red, vibrant red",              "#FF0000");
        put(COLOR_MAP, "大红",  "bright red, chinese red",       "#FF2222");
        put(COLOR_MAP, "深红",  "dark red, crimson",             "#8B0000");
        put(COLOR_MAP, "暗红",  "dark red, burgundy",            "#800000");
        put(COLOR_MAP, "酒红",  "wine red, burgundy",            "#722F37");
        put(COLOR_MAP, "玫红",  "rose red, carmine",             "#FF007F");
        put(COLOR_MAP, "砖红",  "brick red, terracotta",         "#CB4154");
        // 粉系
        put(COLOR_MAP, "粉",    "pink, soft pink",               "#FFB6C1");
        put(COLOR_MAP, "粉红",  "pink, rose pink",               "#FF69B4");
        put(COLOR_MAP, "浅粉",  "light pink, blush pink",        "#FFD1DC");
        put(COLOR_MAP, "裸粉",  "nude pink, barely-there pink",  "#F9D5BB");
        put(COLOR_MAP, "桃粉",  "peach pink, peachy",            "#FFCBA4");
        put(COLOR_MAP, "玫粉",  "dusty rose, mauve pink",        "#E8A0A0");
        // 橙系
        put(COLOR_MAP, "橙",    "orange, vibrant orange",        "#FF6600");
        put(COLOR_MAP, "橘",    "tangerine, orange",             "#F28500");
        put(COLOR_MAP, "橘红",  "orange red, persimmon",         "#FF4500");
        put(COLOR_MAP, "珊瑚",  "coral, coral pink",             "#FF7F7F");
        put(COLOR_MAP, "杏",    "apricot, nude peach",           "#FBCEB1");
        // 黄系
        put(COLOR_MAP, "黄",    "yellow, bright yellow",         "#FFFF00");
        put(COLOR_MAP, "金黄",  "golden yellow, saffron",        "#FFD700");
        put(COLOR_MAP, "姜黄",  "ginger yellow, curry",          "#B5A642");
        put(COLOR_MAP, "柠檬黄","lemon yellow",                  "#FFF44F");
        put(COLOR_MAP, "米黄",  "beige, cream yellow",           "#F5F5DC");
        put(COLOR_MAP, "香槟",  "champagne, ivory gold",         "#F7E7CE");
        put(COLOR_MAP, "奶黄",  "butter yellow, cream",          "#FDF0D5");
        // 绿系
        put(COLOR_MAP, "绿",    "green, fresh green",            "#008000");
        put(COLOR_MAP, "墨绿",  "dark green, forest green",      "#354A21");
        put(COLOR_MAP, "军绿",  "military green, olive green",   "#4B5320");
        put(COLOR_MAP, "草绿",  "grass green, lime green",       "#7CFC00");
        put(COLOR_MAP, "薄荷绿","mint green, spearmint",         "#98FF98");
        put(COLOR_MAP, "橄榄绿","olive green",                   "#808000");
        put(COLOR_MAP, "翠绿",  "emerald green, jade green",     "#00A550");
        put(COLOR_MAP, "豆绿",  "pea green, soft green",         "#93C572");
        // 蓝系
        put(COLOR_MAP, "蓝",    "blue, cobalt blue",             "#0000FF");
        put(COLOR_MAP, "深蓝",  "navy blue, dark blue",          "#003153");
        put(COLOR_MAP, "天蓝",  "sky blue, baby blue",           "#87CEEB");
        put(COLOR_MAP, "宝蓝",  "royal blue, sapphire",          "#4169E1");
        put(COLOR_MAP, "藏青",  "indigo, dark navy",             "#1F305E");
        put(COLOR_MAP, "靛蓝",  "indigo, deep blue",             "#4B0082");
        put(COLOR_MAP, "冰蓝",  "ice blue, powder blue",         "#D0E8FF");
        put(COLOR_MAP, "孔雀蓝","peacock blue, teal",            "#00827F");
        // 紫系
        put(COLOR_MAP, "紫",    "purple, violet",                "#800080");
        put(COLOR_MAP, "深紫",  "deep purple, dark violet",      "#4B0082");
        put(COLOR_MAP, "淡紫",  "light purple, lilac",           "#C8A2C8");
        put(COLOR_MAP, "薰衣草","lavender, lavender purple",     "#E6E6FA");
        put(COLOR_MAP, "丁香紫","lilac, soft purple",            "#C8A2C8");
        // 白系
        put(COLOR_MAP, "白",    "white, pure white",             "#FFFFFF");
        put(COLOR_MAP, "米白",  "cream white, off-white",        "#F5F5F0");
        put(COLOR_MAP, "象牙白","ivory, ivory white",            "#FFFFF0");
        put(COLOR_MAP, "乳白",  "milk white, creamy white",      "#FDFCFA");
        put(COLOR_MAP, "珍珠白","pearl white, lustrous white",   "#F8F8F0");
        // 黑系
        put(COLOR_MAP, "黑",    "black, jet black",              "#000000");
        put(COLOR_MAP, "炭黑",  "charcoal black, dark gray",     "#333333");
        // 灰系
        put(COLOR_MAP, "灰",    "gray, medium gray",             "#808080");
        put(COLOR_MAP, "浅灰",  "light gray, silver gray",       "#C0C0C0");
        put(COLOR_MAP, "深灰",  "dark gray, charcoal",           "#555555");
        put(COLOR_MAP, "银灰",  "silver, metallic gray",         "#AAAAAA");
        put(COLOR_MAP, "烟灰",  "smoky gray, ash gray",          "#888888");
        // 棕系
        put(COLOR_MAP, "棕",    "brown, warm brown",             "#8B4513");
        put(COLOR_MAP, "咖啡",  "coffee brown, mocha",           "#6F4E37");
        put(COLOR_MAP, "驼色",  "camel, tan",                    "#C19A6B");
        put(COLOR_MAP, "骆驼",  "camel, warm tan",               "#C19A6B");
        put(COLOR_MAP, "焦糖",  "caramel, toffee",               "#C68642");
        put(COLOR_MAP, "巧克力","chocolate brown, dark cocoa",   "#7B3F00");
        put(COLOR_MAP, "卡其",  "khaki, khaki beige",            "#C3B091");
    }

    // ========== 风格词汇 ==========
    public static final Map<String, String> STYLE_MAP = new LinkedHashMap<>();
    static {
        STYLE_MAP.put("甜酷", "sweet edgy style, Y2K aesthetic, cute streetwear");
        STYLE_MAP.put("甜辣", "sweet edgy style, Y2K, fashionable");
        STYLE_MAP.put("法式", "French style, Parisian chic, effortlessly elegant");
        STYLE_MAP.put("法国风", "French style, Parisian, chic");
        STYLE_MAP.put("极简", "minimalist fashion, clean lines, understated elegance");
        STYLE_MAP.put("简约", "minimalist, clean, simple design");
        STYLE_MAP.put("新中式", "Chinese contemporary fashion, modern qipao style, traditional Chinese elements");
        STYLE_MAP.put("国风", "Chinese style, hanfu inspired, oriental fashion");
        STYLE_MAP.put("汉服", "hanfu style, traditional Chinese clothing, oriental");
        STYLE_MAP.put("旗袍", "qipao style, Chinese dress, mandarin collar");
        STYLE_MAP.put("街头", "streetwear fashion, urban style, hip hop influence");
        STYLE_MAP.put("街潮", "street fashion, urban cool, trendy streetwear");
        STYLE_MAP.put("复古", "vintage fashion, retro style, classic");
        STYLE_MAP.put("vintage", "vintage aesthetic, retro, classic fashion");
        STYLE_MAP.put("运动", "sporty fashion, athletic wear, activewear");
        STYLE_MAP.put("机能", "techwear, functional fashion, utilitarian");
        STYLE_MAP.put("优雅", "elegant fashion, refined, sophisticated");
        STYLE_MAP.put("高雅", "sophisticated, high-end elegant, graceful");
        STYLE_MAP.put("甜美", "sweet fashion, girly style, kawaii");
        STYLE_MAP.put("少女", "girly style, cute feminine, youthful");
        STYLE_MAP.put("性感", "sexy fashion, glamorous, alluring");
        STYLE_MAP.put("波西米亚", "bohemian style, boho chic, free-spirited");
        STYLE_MAP.put("波西米",  "bohemian, boho fashion");
        STYLE_MAP.put("朋克", "punk fashion, edgy, rock and roll");
        STYLE_MAP.put("哥特", "gothic fashion, dark aesthetic");
        STYLE_MAP.put("学院风", "preppy style, collegiate fashion, academic");
        STYLE_MAP.put("校园", "campus style, student fashion, youthful preppy");
        STYLE_MAP.put("OL", "office lady style, business casual, professional");
        STYLE_MAP.put("通勤", "commuter fashion, work wear, office appropriate");
        STYLE_MAP.put("度假", "resort wear, vacation fashion, holiday style");
        STYLE_MAP.put("海边", "beach fashion, coastal style, resort casual");
        STYLE_MAP.put("森系", "forest girl aesthetic, natural, earthy tones");
        STYLE_MAP.put("仙气", "ethereal fashion, fairy-like, dreamy aesthetic");
        STYLE_MAP.put("高定", "haute couture, high fashion, luxury designer");
        STYLE_MAP.put("轻奢", "contemporary luxury, affordable luxury fashion");
    }

    // ========== 面料词汇 ==========
    public static final Map<String, String> FABRIC_MAP = new LinkedHashMap<>();
    static {
        FABRIC_MAP.put("牛仔", "denim fabric, denim material, jeans texture");
        FABRIC_MAP.put("丹宁", "denim, denim fabric");
        FABRIC_MAP.put("真丝", "silk fabric, pure silk, lustrous silk");
        FABRIC_MAP.put("丝绸", "silk, satin fabric, smooth silk");
        FABRIC_MAP.put("桑蚕丝", "mulberry silk, pure silk fabric");
        FABRIC_MAP.put("缎面", "satin, satin fabric, smooth shiny");
        FABRIC_MAP.put("棉", "cotton fabric, pure cotton, breathable cotton");
        FABRIC_MAP.put("纯棉", "100% cotton, pure cotton fabric");
        FABRIC_MAP.put("全棉", "all cotton, pure cotton");
        FABRIC_MAP.put("针织", "knit fabric, knitwear, ribbed knit");
        FABRIC_MAP.put("毛织", "woven knit, fabric texture");
        FABRIC_MAP.put("蕾丝", "lace fabric, delicate lace, floral lace");
        FABRIC_MAP.put("皮革", "leather fabric, smooth leather");
        FABRIC_MAP.put("皮", "leather, faux leather material");
        FABRIC_MAP.put("雪纺", "chiffon fabric, flowy chiffon, sheer chiffon");
        FABRIC_MAP.put("纱", "sheer fabric, tulle, chiffon");
        FABRIC_MAP.put("麻", "linen fabric, natural linen");
        FABRIC_MAP.put("亚麻", "linen, flax fabric, natural texture");
        FABRIC_MAP.put("羊毛", "wool fabric, warm wool, woolen");
        FABRIC_MAP.put("羊绒", "cashmere, soft cashmere wool");
        FABRIC_MAP.put("开司米", "cashmere, luxury soft wool");
        FABRIC_MAP.put("天鹅绒", "velvet fabric, plush velvet");
        FABRIC_MAP.put("丝绒", "velvet, silk velvet, luxurious velvet");
        FABRIC_MAP.put("灯芯绒", "corduroy fabric, ribbed corduroy");
        FABRIC_MAP.put("格子", "plaid fabric, checked pattern, tartan");
        FABRIC_MAP.put("条纹", "striped fabric, stripe pattern");
        FABRIC_MAP.put("碎花", "floral print, flower pattern fabric");
        FABRIC_MAP.put("印花", "printed fabric, print pattern");
    }

    // ========== 长度词汇 ==========
    public static final Map<String, String> LENGTH_MAP = new LinkedHashMap<>();
    static {
        LENGTH_MAP.put("超短", "ultra short, micro mini length");
        LENGTH_MAP.put("迷你", "mini length, very short");
        LENGTH_MAP.put("短", "short length, cropped");
        LENGTH_MAP.put("中长", "midi length, below knee");
        LENGTH_MAP.put("及膝", "knee length, midi");
        LENGTH_MAP.put("长", "long length, maxi");
        LENGTH_MAP.put("长款", "long style, maxi length");
        LENGTH_MAP.put("拖地", "floor length, with train, maxi gown");
        LENGTH_MAP.put("超长", "floor sweeping length, extra long");
    }

    // ========== 版型词汇 ==========
    public static final Map<String, String> FIT_MAP = new LinkedHashMap<>();
    static {
        FIT_MAP.put("宽松", "oversized fit, loose style, relaxed silhouette");
        FIT_MAP.put("超宽", "extremely oversized, exaggerated loose");
        FIT_MAP.put("廓形", "sculptural silhouette, boxy oversized");
        FIT_MAP.put("修身", "slim fit, body-conscious, fitted silhouette");
        FIT_MAP.put("紧身", "tight fit, bodycon, form-fitting");
        FIT_MAP.put("显瘦", "slimming cut, figure-flattering, streamlined");
        FIT_MAP.put("合体", "regular fit, well-fitted, proper size");
        FIT_MAP.put("A字", "A-line silhouette, flared skirt, A-line cut");
        FIT_MAP.put("直筒", "straight cut, column silhouette");
        FIT_MAP.put("H型", "H-line, boxy straight silhouette");
        FIT_MAP.put("X型", "X silhouette, hourglass shape, waist emphasis");
    }

    // ========== 服装品类 ==========
    public static final Map<String, String> GARMENT_MAP = new LinkedHashMap<>();
    static {
        GARMENT_MAP.put("连衣裙", "dress, one-piece dress");
        GARMENT_MAP.put("裙子", "dress, skirt");
        GARMENT_MAP.put("裙", "dress, skirt");
        GARMENT_MAP.put("半裙", "skirt, A-line skirt");
        GARMENT_MAP.put("短裙", "mini skirt, short skirt");
        GARMENT_MAP.put("上衣", "top, blouse, shirt");
        GARMENT_MAP.put("T恤", "t-shirt, casual top");
        GARMENT_MAP.put("衬衫", "shirt, button-down shirt, blouse");
        GARMENT_MAP.put("外套", "jacket, outerwear, coat");
        GARMENT_MAP.put("大衣", "coat, overcoat, long coat");
        GARMENT_MAP.put("夹克", "jacket, biker jacket");
        GARMENT_MAP.put("风衣", "trench coat, windbreaker coat");
        GARMENT_MAP.put("西装", "blazer, formal jacket, suit jacket");
        GARMENT_MAP.put("西服", "suit, formal wear, business suit");
        GARMENT_MAP.put("卫衣", "hoodie, sweatshirt, pullover");
        GARMENT_MAP.put("毛衣", "sweater, knitwear, pullover knit");
        GARMENT_MAP.put("裤子", "pants, trousers");
        GARMENT_MAP.put("裤", "pants, trousers");
        GARMENT_MAP.put("阔腿裤", "wide leg pants, palazzo trousers");
        GARMENT_MAP.put("套装", "matching set, two-piece suit, coordinates");
        GARMENT_MAP.put("内衣", "lingerie, intimate wear, underwear set");
        GARMENT_MAP.put("泳衣", "swimsuit, swimwear, bathing suit");
        GARMENT_MAP.put("旗袍", "qipao, Chinese dress, cheongsam");
    }

    // ========== 季节/场合关键词 ==========
    public static final Map<String, String> OCCASION_MAP = new LinkedHashMap<>();
    static {
        OCCASION_MAP.put("春夏",  "spring summer collection, lightweight, fresh");
        OCCASION_MAP.put("秋冬",  "fall winter collection, warm, layered");
        OCCASION_MAP.put("婚礼",  "wedding, bridal, ceremony");
        OCCASION_MAP.put("晚宴",  "evening gown, formal dinner, gala");
        OCCASION_MAP.put("派对",  "party wear, cocktail, festive");
        OCCASION_MAP.put("日常",  "casual everyday wear, comfortable daily");
        OCCASION_MAP.put("约会",  "date night outfit, romantic, feminine");
    }

    // ========== 3D 相关关键词 ==========
    public static final List<String> THREE_D_KEYWORDS = List.of(
            "3d", "3D", "三维", "立体", "转3D", "生成3D", "三维模型", "旋转", "多角度"
    );

    // ========== 批量生成数字词 ==========
    public static final Map<String, Integer> BATCH_NUMBER_MAP = new LinkedHashMap<>();
    static {
        BATCH_NUMBER_MAP.put("一", 1);
        BATCH_NUMBER_MAP.put("两", 2);
        BATCH_NUMBER_MAP.put("三", 3);
        BATCH_NUMBER_MAP.put("四", 4);
        BATCH_NUMBER_MAP.put("五", 5);
        BATCH_NUMBER_MAP.put("六", 6);
        BATCH_NUMBER_MAP.put("七", 7);
        BATCH_NUMBER_MAP.put("八", 8);
        BATCH_NUMBER_MAP.put("十", 10);
        BATCH_NUMBER_MAP.put("几", 4);   // 几款 → 默认4
        BATCH_NUMBER_MAP.put("多", 4);   // 多款 → 默认4
        BATCH_NUMBER_MAP.put("一组", 4);
        BATCH_NUMBER_MAP.put("一堆", 6);
        BATCH_NUMBER_MAP.put("很多", 6);
    }

    // ========== 辅助 ==========
    private static void put(Map<String, String[]> map, String cn, String sd, String hex) {
        map.put(cn, new String[]{sd, hex});
    }

}
