package cn.iocoder.yudao.module.product.service.sku;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertNull;

// TODO 芋艿：整合到 {@link ProductSkuServiceTest} 中
/**
* {@link ProductSkuServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(ProductSkuServiceImpl.class)
@Disabled // TODO 芋艿：临时去掉
public class SkuServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProductSkuServiceImpl ProductSkuService;

    @Resource
    private ProductSkuMapper ProductSkuMapper;

    @Test
    public void testDeleteSku_success() {
        // mock 数据
        ProductSkuDO dbSku = randomPojo(ProductSkuDO.class);
        ProductSkuMapper.insert(dbSku);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSku.getId();

        // 调用
        ProductSkuService.deleteSku(id);
       // 校验数据不存在了
       assertNull(ProductSkuMapper.selectById(id));
    }

    @Test
    public void testDeleteSku_notExists() {
        // 准备参数
        Long id = 1L;

        // 调用, 并断言异常
        assertServiceException(() -> ProductSkuService.deleteSku(id), SKU_NOT_EXISTS);
    }

}
