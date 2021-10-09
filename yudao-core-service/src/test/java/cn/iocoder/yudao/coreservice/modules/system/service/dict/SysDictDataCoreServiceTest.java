package cn.iocoder.yudao.coreservice.modules.system.service.dict;

import cn.iocoder.yudao.coreservice.BaseDbUnitTest;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.dict.SysDictDataCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.dict.impl.SysDictDataCoreServiceImpl;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import com.google.common.collect.ImmutableTable;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.function.Consumer;

import static cn.hutool.core.bean.BeanUtil.getFieldValue;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomCommonStatus;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
* {@link SysDictDataCoreServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysDictDataCoreServiceImpl.class)
public class SysDictDataCoreServiceTest extends BaseDbUnitTest {

    @Resource
    private SysDictDataCoreServiceImpl dictDataCoreService;

    @Resource
    private SysDictDataCoreMapper dictDataMapper;

    /**
     * 测试加载到新的字典数据的情况
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testInitLocalCache() {
        // mock 数据
        SysDictDataDO dictData01 = randomDictDataDO();
        dictDataMapper.insert(dictData01);
        SysDictDataDO dictData02 = randomDictDataDO();
        dictDataMapper.insert(dictData02);

        // 调用
        dictDataCoreService.initLocalCache();
        // 断言 labelDictDataCache 缓存
        ImmutableTable<String, String, SysDictDataDO> labelDictDataCache =
                (ImmutableTable<String, String, SysDictDataDO>) getFieldValue(dictDataCoreService, "labelDictDataCache");
        assertEquals(2, labelDictDataCache.size());
        assertPojoEquals(dictData01, labelDictDataCache.get(dictData01.getDictType(), dictData01.getLabel()));
        assertPojoEquals(dictData02, labelDictDataCache.get(dictData02.getDictType(), dictData02.getLabel()));
        // 断言 valueDictDataCache 缓存
        ImmutableTable<String, String, SysDictDataDO> valueDictDataCache =
                (ImmutableTable<String, String, SysDictDataDO>) getFieldValue(dictDataCoreService, "valueDictDataCache");
        assertEquals(2, valueDictDataCache.size());
        assertPojoEquals(dictData01, valueDictDataCache.get(dictData01.getDictType(), dictData01.getValue()));
        assertPojoEquals(dictData02, valueDictDataCache.get(dictData02.getDictType(), dictData02.getValue()));
        // 断言 maxUpdateTime 缓存
        Date maxUpdateTime = (Date) getFieldValue(dictDataCoreService, "maxUpdateTime");
        assertEquals(ObjectUtils.max(dictData01.getUpdateTime(), dictData02.getUpdateTime()), maxUpdateTime);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysDictDataDO randomDictDataDO(Consumer<SysDictDataDO>... consumers) {
        Consumer<SysDictDataDO> consumer = (o) -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
        };
        return randomPojo(SysDictDataDO.class, ArrayUtils.append(consumer, consumers));
    }

}
