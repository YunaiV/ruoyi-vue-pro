package cn.iocoder.yudao.module.erp.service.product.bookcase.bo;

import lombok.Data;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;

/**
 * 书架
 * 
 * @author Wqh
 */
@Data
public class BookcaseBO extends ErpProductBO {
    // 添加字段
    private String name;
    // 其他字段
}
