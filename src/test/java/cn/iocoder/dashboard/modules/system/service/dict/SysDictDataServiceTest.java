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
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.DICT_DATA_NOT_EXISTS;
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
        when(dictTypeService.getDictType(eq(reqVO.getDictType())))
                .thenReturn(randomPojo(SysDictTypeDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus())));

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
    public void testUpdateDictData_success() {
        // mock 数据
        SysDictDataDO dbDictData = randomPojo(SysDictDataDO.class);
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
        });

        // 调用
        dictDataService.updateDictData(reqVO);
        // 校验是否更新正确
        SysDictDataDO dictData = dictDataMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, dictData);
    }

    @Test
    public void testUpdateDictData_notExists() {
        // 准备参数
        SysDictDataUpdateReqVO reqVO = randomPojo(SysDictDataUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.updateDictData(reqVO), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testDeleteDictData_success() {
        // mock 数据
        SysDictDataDO dbDictData = randomPojo(SysDictDataDO.class);
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDictData.getId();

        // 调用
        dictDataService.deleteDictData(id);
       // 校验数据不存在了
       assertNull(dictDataMapper.selectById(id));
    }

    @Test
    public void testDeleteDictData_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.deleteDictData(id), DICT_DATA_NOT_EXISTS);
    }

}
