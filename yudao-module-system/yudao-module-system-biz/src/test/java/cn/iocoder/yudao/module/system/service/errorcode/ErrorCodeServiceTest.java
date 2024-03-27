package cn.iocoder.yudao.module.system.service.errorcode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.api.errorcode.dto.ErrorCodeAutoGenerateReqDTO;
import cn.iocoder.yudao.module.system.api.errorcode.dto.ErrorCodeRespDTO;
import cn.iocoder.yudao.module.system.controller.admin.errorcode.vo.ErrorCodePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.errorcode.vo.ErrorCodeSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.errorcode.ErrorCodeDO;
import cn.iocoder.yudao.module.system.dal.mysql.errorcode.ErrorCodeMapper;
import cn.iocoder.yudao.module.system.enums.errorcode.ErrorCodeTypeEnum;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ERROR_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.ERROR_CODE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

@Import(ErrorCodeServiceImpl.class)
public class ErrorCodeServiceTest extends BaseDbUnitTest {

    @Resource
    private ErrorCodeServiceImpl errorCodeService;

    @Resource
    private ErrorCodeMapper errorCodeMapper;

    @Test
    public void testCreateErrorCode_success() {
        // 准备参数
        ErrorCodeSaveReqVO reqVO = randomPojo(ErrorCodeSaveReqVO.class)
                .setId(null); // 防止 id 被赋值

        // 调用
        Long errorCodeId = errorCodeService.createErrorCode(reqVO);
        // 断言
        assertNotNull(errorCodeId);
        // 校验记录的属性是否正确
        ErrorCodeDO errorCode = errorCodeMapper.selectById(errorCodeId);
        assertPojoEquals(reqVO, errorCode, "id");
        assertEquals(ErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testUpdateErrorCode_success() {
        // mock 数据
        ErrorCodeDO dbErrorCode = randomErrorCodeDO();
        errorCodeMapper.insert(dbErrorCode);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ErrorCodeSaveReqVO reqVO = randomPojo(ErrorCodeSaveReqVO.class, o -> {
            o.setId(dbErrorCode.getId()); // 设置更新的 ID
        });

        // 调用
        errorCodeService.updateErrorCode(reqVO);
        // 校验是否更新正确
        ErrorCodeDO errorCode = errorCodeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, errorCode);
        assertEquals(ErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testDeleteErrorCode_success() {
        // mock 数据
        ErrorCodeDO dbErrorCode = randomErrorCodeDO();
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
       ErrorCodeDO dbErrorCode = initGetErrorCodePage();
       // 准备参数
       ErrorCodePageReqVO reqVO = new ErrorCodePageReqVO();
       reqVO.setType(ErrorCodeTypeEnum.AUTO_GENERATION.getType());
       reqVO.setApplicationName("tu");
       reqVO.setCode(1);
       reqVO.setMessage("ma");
       reqVO.setCreateTime(buildBetweenTime(2020, 11, 1, 2020, 11, 30));

       // 调用
       PageResult<ErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbErrorCode, pageResult.getList().get(0));
    }

    /**
     * 初始化 getErrorCodePage 方法的测试数据
     */
    private ErrorCodeDO initGetErrorCodePage() {
        ErrorCodeDO dbErrorCode = randomErrorCodeDO(o -> { // 等会查询到
            o.setType(ErrorCodeTypeEnum.AUTO_GENERATION.getType());
            o.setApplicationName("tudou");
            o.setCode(1);
            o.setMessage("yuanma");
            o.setCreateTime(buildTime(2020, 11, 11));
        });
        errorCodeMapper.insert(dbErrorCode);
        // 测试 type 不匹配
        errorCodeMapper.insert(cloneIgnoreId(dbErrorCode, o -> o.setType(ErrorCodeTypeEnum.MANUAL_OPERATION.getType())));
        // 测试 applicationName 不匹配
        errorCodeMapper.insert(cloneIgnoreId(dbErrorCode, o -> o.setApplicationName("yuan")));
        // 测试 code 不匹配
        errorCodeMapper.insert(cloneIgnoreId(dbErrorCode, o -> o.setCode(2)));
        // 测试 message 不匹配
        errorCodeMapper.insert(cloneIgnoreId(dbErrorCode, o -> o.setMessage("nai")));
        // 测试 createTime 不匹配
        errorCodeMapper.insert(cloneIgnoreId(dbErrorCode, o -> o.setCreateTime(buildTime(2020, 12, 12))));
        return dbErrorCode;
    }

    @Test
    public void testValidateCodeDuplicate_codeDuplicateForCreate() {
        // 准备参数
        Integer code = randomInteger();
        // mock 数据
        errorCodeMapper.insert(randomErrorCodeDO(o -> o.setCode(code)));

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
        errorCodeMapper.insert(randomErrorCodeDO(o -> o.setCode(code)));

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
        ErrorCodeDO errorCode = errorCodeMapper.selectOne(null);
        assertPojoEquals(generateReqDTO, errorCode);
        assertEquals(ErrorCodeTypeEnum.AUTO_GENERATION.getType(), errorCode.getType());
    }

    /**
     * 情况 2.1，错误码存在，但是是 ErrorCodeTypeEnum.MANUAL_OPERATION 类型
     */
    @Test
    public void testAutoGenerateErrorCodes_021() {
        // mock 数据
        ErrorCodeDO dbErrorCode = randomErrorCodeDO(o -> o.setType(ErrorCodeTypeEnum.MANUAL_OPERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，相等，说明不会更新
        ErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * 情况 2.2，错误码存在，但是是 applicationName 不匹配
     */
    @Test
    public void testAutoGenerateErrorCodes_022() {
        // mock 数据
        ErrorCodeDO dbErrorCode = randomErrorCodeDO(o -> o.setType(ErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(randomString()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，相等，说明不会更新
        ErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * 情况 2.3，错误码存在，但是是 message 相同
     */
    @Test
    public void testAutoGenerateErrorCodes_023() {
        // mock 数据
        ErrorCodeDO dbErrorCode = randomErrorCodeDO(o -> o.setType(ErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(dbErrorCode.getApplicationName())
                    .setMessage(dbErrorCode.getMessage()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，相等，说明不会更新
        ErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(dbErrorCode, errorCode);
    }

    /**
     * 情况 2.3，错误码存在，但是是 message 不同，则进行更新
     */
    @Test
    public void testAutoGenerateErrorCodes_024() {
        // mock 数据
        ErrorCodeDO dbErrorCode = randomErrorCodeDO(o -> o.setType(ErrorCodeTypeEnum.AUTO_GENERATION.getType()));
        errorCodeMapper.insert(dbErrorCode);
        // 准备参数
        ErrorCodeAutoGenerateReqDTO generateReqDTO = randomPojo(ErrorCodeAutoGenerateReqDTO.class,
                o -> o.setCode(dbErrorCode.getCode()).setApplicationName(dbErrorCode.getApplicationName()));
        // mock 方法

        // 调用
        errorCodeService.autoGenerateErrorCodes(Lists.newArrayList(generateReqDTO));
        // 断言，匹配
        ErrorCodeDO errorCode = errorCodeMapper.selectById(dbErrorCode.getId());
        assertPojoEquals(generateReqDTO, errorCode);
    }

    @Test
    public void testGetErrorCode() {
        // 准备参数
        ErrorCodeDO errorCodeDO = randomErrorCodeDO();
        errorCodeMapper.insert(errorCodeDO);
        // mock 方法
        Long id = errorCodeDO.getId();

        // 调用
        ErrorCodeDO dbErrorCode = errorCodeService.getErrorCode(id);
        // 断言
        assertPojoEquals(errorCodeDO, dbErrorCode);
    }

    @Test
    public void testGetErrorCodeList() {
        // 准备参数
        ErrorCodeDO errorCodeDO01 = randomErrorCodeDO(
                o -> o.setApplicationName("yunai_server").setUpdateTime(buildTime(2022, 1, 10)));
        errorCodeMapper.insert(errorCodeDO01);
        ErrorCodeDO errorCodeDO02 = randomErrorCodeDO(
                o -> o.setApplicationName("yunai_server").setUpdateTime(buildTime(2022, 1, 12)));
        errorCodeMapper.insert(errorCodeDO02);
        // mock 方法
        String applicationName = "yunai_server";
        LocalDateTime minUpdateTime = buildTime(2022, 1, 11);

        // 调用
        List<ErrorCodeRespDTO> errorCodeList = errorCodeService.getErrorCodeList(applicationName, minUpdateTime);
        // 断言
        assertEquals(1, errorCodeList.size());
        assertPojoEquals(errorCodeDO02, errorCodeList.get(0));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static ErrorCodeDO randomErrorCodeDO(Consumer<ErrorCodeDO>... consumers) {
        Consumer<ErrorCodeDO> consumer = (o) -> {
            o.setType(randomEle(ErrorCodeTypeEnum.values()).getType()); // 保证 key 的范围
        };
        return randomPojo(ErrorCodeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
