package cn.iocoder.yudao.adminserver.modules.system.service.errorcode;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.tool.framework.errorcode.core.dto.ErrorCodeAutoGenerateReqDTO;
import cn.iocoder.yudao.adminserver.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodeCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodeExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodePageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.SysErrorCodeUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.errorcode.SysErrorCodeDO;
import cn.iocoder.yudao.adminserver.modules.system.dal.mysql.errorcode.SysErrorCodeMapper;
import cn.iocoder.yudao.adminserver.modules.system.enums.errorcode.SysErrorCodeTypeEnum;
import cn.iocoder.yudao.adminserver.modules.system.service.errorcode.impl.SysErrorCodeServiceImpl;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_DUPLICATE;
import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.ERROR_CODE_NOT_EXISTS;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link SysErrorCodeServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(SysErrorCodeServiceImpl.class)
public class SysErrorCodeServiceTest extends BaseDbUnitTest {

    @Resource
    private SysErrorCodeServiceImpl errorCodeService;

    @Resource
    private SysErrorCodeMapper errorCodeMapper;

    @Mock
    private Logger log;

    @Test
    public void testCreateErrorCode_success() {
        // 准备参数
        SysErrorCodeCreateReqVO reqVO = randomPojo(SysErrorCodeCreateReqVO.class);

        // 调用
        Long errorCodeId = errorCodeService.createErrorCode(reqVO);
        // 断言
        assertNotNull(errorCodeId);
        // 校验记录的属性是否正确
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(errorCodeId);
        assertPojoEquals(reqVO, errorCode);
        assertEquals(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testUpdateErrorCode_success() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO();
        errorCodeMapper.insert(dbErrorCode);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SysErrorCodeUpdateReqVO reqVO = randomPojo(SysErrorCodeUpdateReqVO.class, o -> {
            o.setId(dbErrorCode.getId()); // 设置更新的 ID
        });

        // 调用
        errorCodeService.updateErrorCode(reqVO);
        // 校验是否更新正确
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, errorCode);
        assertEquals(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testDeleteErrorCode_success() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO();
        errorCodeMapper.insert(dbErrorCode);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbErrorCode.getId();

        // 调用
        errorCodeService.deleteErrorCode(id);
       // 校验数据不存在了
       assertNull(errorCodeMapper.selectById(id));
    }

    @Test
    public void testGetErrorCodePage() {
       // mock 数据
       SysErrorCodeDO dbErrorCode = initGetErrorCodePage();
       // 准备参数
       SysErrorCodePageReqVO reqVO = new SysErrorCodePageReqVO();
       reqVO.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
       reqVO.setApplicationName("yudao");
       reqVO.setCode(1);
       reqVO.setMessage("yu");
       reqVO.setBeginCreateTime(buildTime(2020, 11, 1));
       reqVO.setEndCreateTime(buildTime(2020, 11, 30));

       // 调用
       PageResult<SysErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbErrorCode, pageResult.getList().get(0));
    }

    /**
     * 初始化 getErrorCodePage 方法的测试数据
     */
    private SysErrorCodeDO initGetErrorCodePage() {
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> { // 等会查询到
            o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
            o.setApplicationName("yudaoyuanma");
            o.setCode(1);
            o.setMessage("yudao");
            o.setCreateTime(buildTime(2020, 11, 11));
        });
        errorCodeMapper.insert(dbErrorCode);
        // 测试 type 不匹配
        errorCodeMapper.insert(ObjectUtils.cloneIgnoreId(dbErrorCode, o -> o.setType(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType())));
        // 测试 applicationName 不匹配
        errorCodeMapper.insert(ObjectUtils.cloneIgnoreId(dbErrorCode, o -> o.setApplicationName("yunai")));
        // 测试 code 不匹配
        errorCodeMapper.insert(ObjectUtils.cloneIgnoreId(dbErrorCode, o -> o.setCode(2)));
        // 测试 message 不匹配
        errorCodeMapper.insert(ObjectUtils.cloneIgnoreId(dbErrorCode, o -> o.setMessage("nai")));
        // 测试 createTime 不匹配
        errorCodeMapper.insert(ObjectUtils.cloneIgnoreId(dbErrorCode, o -> o.setCreateTime(buildTime(2020, 12, 12))));
        return dbErrorCode;
    }

    @Test
    public void testGetErrorCodeList() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = initGetErrorCodePage();
        // 准备参数
        SysErrorCodeExportReqVO reqVO = new SysErrorCodeExportReqVO();
        reqVO.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType());
        reqVO.setApplicationName("yudao");
        reqVO.setCode(1);
        reqVO.setMessage("yu");
        reqVO.setBeginCreateTime(buildTime(2020, 11, 1));
        reqVO.setEndCreateTime(buildTime(2020, 11, 30));

        // 调用
        List<SysErrorCodeDO> list = errorCodeService.getErrorCodeList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbErrorCode, list.get(0));
    }

    @Test
    public void testValidateCodeDuplicate_codeDuplicateForCreate() {
        // 准备参数
        Integer code = randomInteger();
        // mock 数据
        errorCodeMapper.insert(randomInfErrorCodeDO(o -> o.setCode(code)));

        // 调用，校验异常
        assertServiceException(() -> errorCodeService.validateCodeDuplicate(code, null),
                ERROR_CODE_DUPLICATE);
    }

    @Test
    public void testValidateCodeDuplicate_codeDuplicateForUpdate() {
        // 准备参数
        Long id = randomLongId();
        Integer code = randomInteger();
        // mock 数据
        errorCodeMapper.insert(randomInfErrorCodeDO(o -> o.setCode(code)));

        // 调用，校验异常
        assertServiceException(() -> errorCodeService.validateCodeDuplicate(code, id),
                ERROR_CODE_DUPLICATE);
    }

    @Test
    public void testValidateErrorCodeExists_notExists() {
        assertServiceException(() -> errorCodeService.validateErrorCodeExists(null),
                ERROR_CODE_NOT_EXISTS);
    }

    /**
     * 情况 1，错误码不存在的情况
     */
    @Test
    public void testAutoGenerateErrorCodes_01() {
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class);
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言
        SysErrorCodeDO errorCode = errorCodeMapper.selectOne(null);
        assertPojoEquals(generateReqDTO, errorCode);
        assertEquals(SysErrorCodeTypeEnum.AUTO_GENERATION.getType(), errorCode.getType());
    }

    /**
     * 情况 2.1，错误码存在，但是是 SysErrorCodeTypeEnum.MANUAL_OPERATION 类型
     */
    @Test
    public void testAutoGenerateErrorCodes_021() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.MANUAL_OPERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，相等，说明不会更新
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * 情况 2.2，错误码存在，但是是 applicationName 不匹配
     */
    @Test
    public void testAutoGenerateErrorCodes_022() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(randomString()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，相等，说明不会更新
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * 情况 2.3，错误码存在，但是是 message 相同
     */
    @Test
    public void testAutoGenerateErrorCodes_023() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(dbErrorCode.getApplicationName())
                    .setMessage(dbErrorCode.getMessage()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，相等，说明不会更新
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * 情况 2.3，错误码存在，但是是 message 不同，则进行更新
     */
    @Test
    public void testAutoGenerateErrorCodes_024() {
        // mock 数据
        SysErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> o.setType(SysErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(dbErrorCode.getApplicationName()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，匹配
        SysErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(generateReqDTO, errorCode);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static SysErrorCodeDO randomInfErrorCodeDO(Consumer<SysErrorCodeDO>... consumers) {
        Consumer<SysErrorCodeDO> consumer = (o) -> {
            o.setType(randomEle(InfConfigTypeEnum.values()).getType()); // 保证 key 的范围
        };
        return randomPojo(SysErrorCodeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
