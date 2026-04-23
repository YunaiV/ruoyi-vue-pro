package cn.iocoder.yudao.module.deepay.agent;

import lombok.Data;

/**
 * 设计图评分结果 VO（ImageScoringAgent 产出）。
 */
@Data
public class DesignImage {
    private String url;
    private double score;
    private String category;
    private String style;
    private double trendScore;
    private double matchScore;
}
