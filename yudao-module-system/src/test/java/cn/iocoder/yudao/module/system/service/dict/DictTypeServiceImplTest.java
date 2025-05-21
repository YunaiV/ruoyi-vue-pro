package cn.iocoder.yudao.module.system.service.dict;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.type.DictTypeSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictTypeDO;
import cn.iocoder.yudao.module.system.dal.mysql.dict.DictTypeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(DictTypeServiceImpl.class)
public class DictTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DictTypeServiceImpl dictTypeService;

    @Resource
    private DictTypeMapper dictTypeMapper;
    @MockBean
    private DictDataService dictDataService;

    @Test
    public void testGetDictTypePage() {
       // mock 数据
       DictTypeDO dbDictType = randomPojo(DictTypeDO.class, o -> { // 等会查询到
           o.setName("yunai");
           o.setType("芋艿");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setCreateTime(buildTime(2021, 1, 15));
       });
       dictTypeMapper.insert(dbDictType);
       // 测试 name 不匹配
       dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setName("tudou")));
       // 测试 type 不匹配
       dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setType("土豆")));
       // 测试 status 不匹配
       dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 createTime 不匹配
       dictTypeMapper.insert(cloneIgnoreId(dbDictType, o -> o.setCreateTime(buildTime(2021, 1, 1))));
       // 准备参数
       DictTypePageReqVO reqVO = new DictTypePageReqVO();
       reqVO.setName("nai");
       reqVO.setType("艿");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setCreateTime(buildBetweenTime(2021, 1, 10, 2021, 1, 20));

       // 调用
       PageResult<DictTypeDO> pageResult = dictTypeService.getDictTypePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDictType, pageResult.getList().get(0));
    }

    @Test
    public void testGetDictType_id() {
        // mock 数据
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);
        // 准备参数
        Long id = dbDictType.getId();

        // 调用
        DictTypeDO dictType = dictTypeService.getDictType(id);
        // 断言
        assertNotNull(dictType);
        assertPojoEquals(dbDictType, dictType);
    }

    @Test
    public void testGetDictType_type() {
        // mock 数据
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);
        // 准备参数
        String type = dbDictType.getType();

        // 调用
        DictTypeDO dictType = dictTypeService.getDictType(type);
        // 断言
        assertNotNull(dictType);
        assertPojoEquals(dbDictType, dictType);
    }

    @Test
    public void testCreateDictType_success() {
        // 准备参数
        DictTypeSaveReqVO reqVO = randomPojo(DictTypeSaveReqVO.class,
                o -> o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()))
                .setId(null); // 避免 id 被赋值

        // 调用
        Long dictTypeId = dictTypeService.createDictType(reqVO);
        // 断言
        assertNotNull(dictTypeId);
        // 校验记录的属性是否正确
        DictTypeDO dictType = dictTypeMapper.selectById(dictTypeId);
        assertPojoEquals(reqVO, dictType, "id");
    }

    @Test
    public void testUpdateDictType_success() {
        // mock 数据
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DictTypeSaveReqVO reqVO = randomPojo(DictTypeSaveReqVO.class, o -> {
            o.setId(dbDictType.getId()); // 设置更新的 ID
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus());
        });

        // 调用
        dictTypeService.updateDictType(reqVO);
        // 校验是否更新正确
        DictTypeDO dictType = dictTypeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, dictType);
    }

    @Test
    public void testDeleteDictType_success() {
        // mock 数据
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDictType.getId();

        // 调用
        dictTypeService.deleteDictType(id);
        // 校验数据不存在了
        assertNull(dictTypeMapper.selectById(id));
    }

    @Test
    public void testDeleteDictType_hasChildren() {
        // mock 数据
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDictType.getId();
        // mock 方法
        when(dictDataService.getDictDataCountByDictType(eq(dbDictType.getType()))).thenReturn(1L);

        // 调用, 并断言异常
        assertServiceException(() -> dictTypeService.deleteDictType(id), DICT_TYPE_HAS_CHILDREN);
    }

    @Test
    public void testGetDictTypeList() {
        // 准备参数
        DictTypeDO dictTypeDO01 = randomDictTypeDO();
        dictTypeMapper.insert(dictTypeDO01);
        DictTypeDO dictTypeDO02 = randomDictTypeDO();
        dictTypeMapper.insert(dictTypeDO02);
        // mock 方法

        // 调用
        List<DictTypeDO> dictTypeDOList = dictTypeService.getDictTypeList();
        // 断言
        assertEquals(2, dictTypeDOList.size());
        assertPojoEquals(dictTypeDO01, dictTypeDOList.get(0));
        assertPojoEquals(dictTypeDO02, dictTypeDOList.get(1));
    }

    @Test
    public void testValidateDictDataExists_success() {
        // mock 数据
        DictTypeDO dbDictType = randomDictTypeDO();
        dictTypeMapper.insert(dbDictType);// @Sql: 先插入出一条存在的数据

        // 调用成功
        dictTypeService.validateDictTypeExists(dbDictType.getId());
    }

    @Test
    public void testValidateDictDataExists_notExists() {
        assertServiceException(() -> dictTypeService.validateDictTypeExists(randomLongId()), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeUnique_success() {
        // 调用，成功
        dictTypeService.validateDictTypeUnique(randomLongId(), randomString());
    }

    @Test
    public void testValidateDictTypeUnique_valueDuplicateForCreate() {
        // 准备参数
        String type = randomString();
        // mock 数据
        dictTypeMapper.insert(randomDictTypeDO(o -> o.setType(type)));

        // 调用，校验异常
        assertServiceException(() -> dictTypeService.validateDictTypeUnique(null, type),
                DICT_TYPE_TYPE_DUPLICATE);
    }

    @Test
    public void testValidateDictTypeUnique_valueDuplicateForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String type = randomString();
        // mock 数据
        dictTypeMapper.insert(randomDictTypeDO(o -> o.setType(type)));

        // 调用，校验异常
        assertServiceException(() -> dictTypeService.validateDictTypeUnique(id, type),
                DICT_TYPE_TYPE_DUPLICATE);
    }

    @Test
    public void testValidateDictTypNameUnique_success() {
        // 调用，成功
        dictTypeService.validateDictTypeNameUnique(randomLongId(), randomString());
    }

    @Test
    public void testValidateDictTypeNameUnique_nameDuplicateForCreate() {
        // 准备参数
        String name = randomString();
        // mock 数据
        dictTypeMapper.insert(randomDictTypeDO(o -> o.setName(name)));

        // 调用，校验异常
        assertServiceException(() -> dictTypeService.validateDictTypeNameUnique(null, name),
                DICT_TYPE_NAME_DUPLICATE);
    }

    @Test
    public void testValidateDictTypeNameUnique_nameDuplicateForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String name = randomString();
        // mock 数据
        dictTypeMapper.insert(randomDictTypeDO(o -> o.setName(name)));

        // 调用，校验异常
        assertServiceException(() -> dictTypeService.validateDictTypeNameUnique(id, name),
                DICT_TYPE_NAME_DUPLICATE);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static DictTypeDO randomDictTypeDO(Consumer<DictTypeDO>... consumers) {
        Consumer<DictTypeDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // 保证 status 的范围
        };
        return randomPojo(DictTypeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
