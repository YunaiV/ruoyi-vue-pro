package cn.iocoder.yudao.module.deepay.agent;

/**
 * 选款推送单元 — SelectionFeedAgent 输出的富类型条目。
 *
 * <pre>
 * {
 *   "image":  "img/trend/coat_workwear_male_1.jpg",
 *   "brand":  "ZARA",
 *   "score":  92,
 *   "reason": "TikTok爆款+欧美市场+工装风格"
 * }
 * </pre>
 *
 * <p>设计师 / 客户看到的是这个结构，而非原始 URL 列表。</p>
 */
public class SelectionFeedItem {

    /** 参考图 URL */
    private String image;

    /** 来源品牌或平台（ZARA / H&M / Nike / TikTok / SHEIN / runway…） */
    private String brand;

    /** 综合热度评分（0~100） */
    private int score;

    /** 推荐理由（供设计师理解为何推这张） */
    private String reason;

    public SelectionFeedItem() {}

    public SelectionFeedItem(String image, String brand, int score, String reason) {
        this.image  = image;
        this.brand  = brand;
        this.score  = score;
        this.reason = reason;
    }

    public String getImage()  { return image; }
    public void   setImage(String image)    { this.image = image; }

    public String getBrand()  { return brand; }
    public void   setBrand(String brand)    { this.brand = brand; }

    public int    getScore()  { return score; }
    public void   setScore(int score)       { this.score = score; }

    public String getReason() { return reason; }
    public void   setReason(String reason)  { this.reason = reason; }

    @Override
    public String toString() {
        return "SelectionFeedItem{brand=" + brand + ", score=" + score + ", image=" + image + "}";
    }
}
