package cn.iocoder.yudao.module.wms.dal.mysql.stock.logic;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.WmsStockLogicDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

@Disabled
class WmsStockLogicMapperTest extends BaseDbUnitTest {
    @Resource
    WmsStockLogicMapper wmsStockLogicMapper;

    @Test
    void selectByDeptIdAndProductIdAndCountryId() {
        List<WmsStockLogicDO> cn = wmsStockLogicMapper.selectByDeptIdAndProductIdAndCountryId(1L, List.of(2L), "CN");
    }
}