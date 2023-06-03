package cn.iocoder.yudao.module.product.enums;

/**
 * Product 常量 TODO 把使用到的常量收拢到一块定义替换魔法值
 *
 * @author HUIHUI
 */
public interface ProductConstants {

    /**
     * 警戒库存 TODO 警戒库存暂时为 10，后期需要使用常量或者数据库配置替换
     */
    int ALERT_STOCK = 10;

    /**
     * 默认商品销量  TODO 默认商品销量为零
     */
    Integer SALES_COUNT = 0;
    /**
     * 默认善品浏览量  TODO 默认浏览量为零
     */
    Integer BROWSE_COUNT = 0;

}
