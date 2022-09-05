package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.controller.admin.property.vo.ProductPropertyViewRespVO;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.ProductSkuRespVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

// TODO @Luowenfeng：这个类只返回 SPU 相关的信息，删除 skus、categoryIds、productPropertyViews；明细使用 SpuDetailRespVO 替代
@ApiModel("管理后台 - 商品 SPU Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuRespVO extends ProductSpuBaseVO {

    @ApiModelProperty(value = "主键", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * SKU 数组
     */
    @ApiModelProperty(value = "sku 数组", example = "[{\"spuId\":4,\"properties\":[{\"propertyId\":3,\"valueId\":15},{\"propertyId\":2,\"valueId\":10}],\"price\":12,\"originalPrice\":32,\"costPrice\":22,\"barCode\":\"765670123123\",\"picUrl\":\"http://test.yudao.iocoder.cn/72938088f1ca8438837c3b51394aea43.jpg\",\"status\":0,\"id\":7,\"createTime\":1656683270000},{\"spuId\":4,\"properties\":[{\"propertyId\":3,\"valueId\":15},{\"propertyId\":2,\"valueId\":11}],\"price\":13,\"originalPrice\":33,\"costPrice\":23,\"barCode\":\"888788770999\",\"picUrl\":\"http://test.yudao.iocoder.cn/6b902c700e5d32e862b6fd9af2e1c0e4.jpg\",\"status\":0,\"id\":8,\"createTime\":1656683270000},{\"spuId\":4,\"properties\":[{\"propertyId\":3,\"valueId\":16},{\"propertyId\":2,\"valueId\":10}],\"price\":14,\"originalPrice\":34,\"costPrice\":24,\"barCode\":\"9999981212\",\"picUrl\":\"http://test.yudao.iocoder.cn/eddf3c79b1917160d94d05244e1f47da.jpg\",\"status\":0,\"id\":9,\"createTime\":1656683270000},{\"spuId\":4,\"properties\":[{\"propertyId\":3,\"valueId\":16},{\"propertyId\":2,\"valueId\":11}],\"price\":15,\"originalPrice\":35,\"costPrice\":25,\"barCode\":\"4444121212\",\"picUrl\":\"http://test.yudao.iocoder.cn/88ac3eb068ea9cfac4726879b2776ccf.jpg\",\"status\":0,\"id\":10,\"createTime\":1656683270000}]")
    private List<ProductSkuRespVO> skus;

    @ApiModelProperty(value = "分类id数组，一直递归到一级父节点", example = "[1,2,4]")
    private List<Long> categoryIds;

    // TODO @芋艿：再琢磨下 这个 VO 类，其实变成 SpuRespVO 内嵌的 VO 类会更好一点；然后把 SpuRespVO 改成 SpuDetailSpuVO

    @ApiModelProperty(value = "规格属性修改和详情展示组合", example = "[{\"propertyId\":2,\"name\":\"内存\",\"propertyValues\":[{\"v1\":11,\"v2\":\"64G\"},{\"v1\":10,\"v2\":\"32G\"}]},{\"propertyId\":3,\"name\":\"尺寸\",\"propertyValues\":[{\"v1\":16,\"v2\":\"6.1\"},{\"v1\":15,\"v2\":\"5.7\"}]}]")
    private List<ProductPropertyViewRespVO> productPropertyViews;

}
