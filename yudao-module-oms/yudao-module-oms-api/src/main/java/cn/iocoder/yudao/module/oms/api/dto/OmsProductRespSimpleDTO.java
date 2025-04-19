package cn.iocoder.yudao.module.oms.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OmsProductRespSimpleDTO {

    /***
     * 产品编号
     */
    private Long id;

    /***
     * 产品名称
     */
    private String name;

    /***
     * sku编码
     */
    private String barCode;


    /***
     * 单位名称
     */
    private String unitName;

    /***
     *品牌
     */
    private String brand;

    /***
     *材料（中文）
     */
    private String material;

    /***
     *主图
     */
    private String primaryImageUrl;
}
