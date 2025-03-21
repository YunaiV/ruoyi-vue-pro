package cn.iocoder.yudao.module.srm.service.product.bookcase.bo;

import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import lombok.Data;

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
