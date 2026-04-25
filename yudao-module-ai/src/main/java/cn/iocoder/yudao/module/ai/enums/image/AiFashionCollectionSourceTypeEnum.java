package cn.iocoder.yudao.module.ai.enums.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 服装设计素材采集源类型枚举
 *
 * @author deepay
 */
@Getter
@AllArgsConstructor
public enum AiFashionCollectionSourceTypeEnum {

    FASHION_SHOW("fashion_show", "时装秀"),
    BRAND("brand", "品牌官网"),
    MODEL_AGENCY("model_agency", "模特机构"),
    STREET_STYLE("street_style", "街拍");

    private final String type;
    private final String desc;

    public static AiFashionCollectionSourceTypeEnum of(String type) {
        for (AiFashionCollectionSourceTypeEnum e : values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }
}
