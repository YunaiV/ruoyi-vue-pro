package cn.iocoder.yudao.module.erp.dal.supporting;

import cn.iocoder.yudao.module.erp.service.product.tvstand.ErpProductTvStandServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Wqh
 * @date: 2024/12/11 13:20
 * @description: java层虚拟表初始化
 */
public class VirtualTableInit {
    private VirtualTableInit() {}

    // 静态代码块初始化数据
    private static final Map<Long, Class<?>> VIRTUAL_TABLE;
    static {
        VIRTUAL_TABLE = new HashMap<>(16);
        VIRTUAL_TABLE.put(87L, ErpProductTvStandServiceImpl.class);
    }

    public static Map<Long, Class<?>> getTableMap() {
        return VIRTUAL_TABLE;
    }
}
