package cn.iocoder.dashboard.modules.system.service.dict;

import cn.iocoder.dashboard.BaseSpringBootUnitTest;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.data.SysDictDataUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dict.SysDictDataDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dict.SysDictTypeDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dict.SysDictDataMapper;
import cn.iocoder.dashboard.modules.system.mq.producer.dict.SysDictDataProducer;
import cn.iocoder.dashboard.modules.system.service.dict.impl.SysDictDataServiceImpl;
import cn.iocoder.dashboard.util.collection.ArrayUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.AssertUtils.assertServiceException;
import static cn.iocoder.dashboard.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
* {@link SysDictDataServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
public class SysDictDataServiceTest extends BaseSpringBootUnitTest {

    @Resource
    private SysDictDataServiceImpl dictDataService;

    @Resource
    private SysDictDataMapper dictDataMapper;
    @MockBean
    private SysDictTypeService dictTypeService;
    @MockBean
    private SysDictDataProducer dictDataProducer;

    @Test
    public void testGetDictDataPage() {
        // mock 数据
        SysDictDataDO dbDictData = randomPojo(SysDictDataDO.class, o -> { // 等会查询到
            o.setLabel("芋艿");
            o.setDictType("yunai");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        // 测试 label 不匹配
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setLabel("艿")));
        // 测试 dictType 不匹配
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setDictType("nai")));
        // 测试 status 不匹配
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        SysDictDataPageReqVO reqVO = new SysDictDataPageReqVO();
        reqVO.setLabel("芋");
        reqVO.setDictType("yu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<SysDictDataDO> pageResult = dictDataService.getDictDataPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDictData, pageResult.getList().get(0));
    }

    @Test
    public void testGetDictDataList() {
        // mock 数据
        SysDictDataDO dbDictData = randomPojo(SysDictDataDO.class, o -> { // 等会查询到
            o.setLabel("芋艿");
            o.setDictType("yunai");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        // 测试 label 不匹配
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setLabel("艿")));
        // 测试 dictType 不匹配
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setDictType("nai")));
        // 测试 status 不匹配
        dictDataMapper.insert(ObjectUtils.clone(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        SysDictDataExportReqVO reqVO = new SysDictDataExportReqVO();
        reqVO.setLabel("芋");
        reqVO.setDictType("yu");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        List<SysDictDataDO> list = dictDataService.getDictDataList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbDictData, list.get(0));
    }

    @Test
    public void testCreateDictData_success() {
        // 准备参数
        SysDictDataCreateReqVO reqVO = randomPojo(SysDictDataCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));
        // mock 方法
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));

        // 调用
        Long dictDataId = dictDataService.createDictData(reqVO);
        // 断言
        assertNotNull(dictDataId);
        // 校验记录的属性是否正确
        SysDictDataDO dictData = dictDataMapper.selectById(dictDataId);
        assertPojoEquals(reqVO, dictData);
        // 校验调用
        verify(dictDataProducer, times(1)).sendDictDataRefreshMessage();
    }

    @Test
    public void testCreateDictData_dictTypeNotExists() {
        // 准备参数
        SysDictDataCreateReqVO reqVO = randomPojo(SysDictDataCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.createDictData(reqVO), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testCreateDictData_dictTypeNotEnable() {
        // 准备参数
        SysDictDataCreateReqVO reqVO = randomPojo(SysDictDataCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));
        // mock 方法，数据类型被禁用
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(
                randomPojo(SysDictTypeDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.createDictData(reqVO), DICT_TYPE_NOT_ENABLE);
    }

    @Test
    public void testCreateDictData_dictDataValueDuplicate() {
        // 准备参数
        SysDictDataCreateReqVO reqVO = randomPojo(SysDictDataCreateReqVO.class,
                o -> o.setStatus(randomCommonStatus()));
        // mock 方法，字典类型
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));
        // mock dictData 重复 value 重复
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(reqVO.getDictType());
            o.setValue(reqVO.getValue()); // 使用 reqVO 的 value，实现重复
        }));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.createDictData(reqVO), DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testUpdateDictData_success() {
        // mock 数据
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });
        // mock 方法，字典类型
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));

        // 调用
        dictDataService.updateDictData(reqVO);
        // 校验是否更新正确
        SysDictDataDO dictData = dictDataMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, dictData);
        // 校验调用
        verify(dictDataProducer, times(1)).sendDictDataRefreshMessage();
    }

    @Test
    public void testUpdateDictData_notExists() {
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.updateDictData(reqVO), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testUpdateDictData_dictTypeNotExists() {
        // mock 数据
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.updateDictData(reqVO), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testUpdateDictData_dictTypeNotEnable() {
        // mock 数据
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });
        // mock 方法，数据类型被禁用
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(
                randomPojo(SysDictTypeDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.updateDictData(reqVO), DICT_TYPE_NOT_ENABLE);
    }

    @Test
    public void testUpdateDictData_dictDataValueDuplicate() {
        // mock 数据
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });
        // mock 方法，字典类型
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));
        // mock dictData 重复 value 重复
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(reqVO.getDictType());
            o.setValue(reqVO.getValue()); // 使用 reqVO 的 value，实现重复
        }));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.updateDictData(reqVO), DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testDeleteDictData_success() {
        // mock 数据
        SysDictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDictData.getId();

        // 调用
        dictDataService.deleteDictData(id);
        // 校验数据不存在了
        assertNull(dictDataMapper.selectById(id));
        // 校验调用
        verify(dictDataProducer, times(1)).sendDictDataRefreshMessage();
    }

    @Test
    public void testDeleteDictData_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.deleteDictData(id), DICT_DATA_NOT_EXISTS);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysDictDataDO randomDictDataDO(Consumer<SysDictDataDO>... consumers) {
        Consumer<SysDictDataDO> consumer = (o) -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
        };
        return randomPojo(SysDictDataDO.class, ArrayUtils.append(consumer, consumers));
    }

    /**
     * 生成一个有效的字典类型
     *
     * @param type 字典类型
     * @return SysDictTypeDO 对象
     */
    private static SysDictTypeDO randomDictTypeDO(String type) {
        return randomPojo(SysDictTypeDO.class, o -> {
            o.setType(type);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 保证 status 是开启
        });
    }

}
