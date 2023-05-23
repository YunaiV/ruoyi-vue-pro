package cn.iocoder.yudao.module.product.enums;

/**
 * Product 常量 TODO 把使用到的常量收拢到一块定义替换魔法值
 *
 * @author HUIHUI
 */
public interface ProductConstants {

    // TODO @puhui999：这个变量，可以放到 CategoryDO 的实体里
    /**
     * 父分类编号 - 根分类
     */
    Long PARENT_ID_NULL = 0L;
    /**
     * 限定分类层级
     */
    int CATEGORY_LEVEL = 2;

    // TODO @puhui999：这个变量，必要项不大哈
    /**
     * SPU 分页 tab 个数
     */
    int SPU_TAB_COUNTS = 5;

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
