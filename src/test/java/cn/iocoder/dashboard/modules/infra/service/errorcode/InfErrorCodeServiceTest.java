package cn.iocoder.dashboard.modules.infra.service.errorcode;

import cn.iocoder.dashboard.BaseDbUnitTest;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodePageReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;
import cn.iocoder.dashboard.modules.infra.dal.mysql.errorcode.InfErrorCodeMapper;
import cn.iocoder.dashboard.modules.infra.enums.config.InfConfigTypeEnum;
import cn.iocoder.dashboard.modules.infra.enums.errorcode.InfErrorCodeTypeEnum;
import cn.iocoder.dashboard.modules.infra.service.errorcode.impl.InfErrorCodeServiceImpl;
import cn.iocoder.dashboard.util.collection.ArrayUtils;
import cn.iocoder.dashboard.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.ERROR_CODE_DUPLICATE;
import static cn.iocoder.dashboard.modules.infra.enums.InfErrorCodeConstants.ERROR_CODE_NOT_EXISTS;
import static cn.iocoder.dashboard.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.dashboard.util.AssertUtils.assertServiceException;
import static cn.iocoder.dashboard.util.RandomUtils.*;
import static cn.iocoder.dashboard.util.date.DateUtils.buildTime;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link InfErrorCodeServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(InfErrorCodeServiceImpl.class)
public class InfErrorCodeServiceTest extends BaseDbUnitTest {

    @Resource
    private InfErrorCodeServiceImpl errorCodeService;

    @Resource
    private InfErrorCodeMapper errorCodeMapper;

    @Test
    public void testCreateErrorCode_success() {
        // 准备参数
        InfErrorCodeCreateReqVO reqVO = randomPojo(InfErrorCodeCreateReqVO.class);

        // 调用
        Long errorCodeId = errorCodeService.createErrorCode(reqVO);
        // 断言
        assertNotNull(errorCodeId);
        // 校验记录的属性是否正确
        InfErrorCodeDO errorCode = errorCodeMapper.selectById(errorCodeId);
        assertPojoEquals(reqVO, errorCode);
        assertEquals(InfErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testUpdateErrorCode_success() {
        // mock 数据
        InfErrorCodeDO dbErrorCode = randomInfErrorCodeDO();
        errorCodeMapper.insert(dbErrorCode);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InfErrorCodeUpdateReqVO reqVO = randomPojo(InfErrorCodeUpdateReqVO.class, o -> {
            o.setId(dbErrorCode.getId()); // 设置更新的 ID
        });

        // 调用
        errorCodeService.updateErrorCode(reqVO);
        // 校验是否更新正确
        InfErrorCodeDO errorCode = errorCodeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, errorCode);
        assertEquals(InfErrorCodeTypeEnum.MANUAL_OPERATION.getType(), errorCode.getType());
    }

    @Test
    public void testDeleteErrorCode_success() {
        // mock 数据
        InfErrorCodeDO dbErrorCode = randomInfErrorCodeDO();
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
       InfErrorCodeDO dbErrorCode = initGetErrorCodePage();
       // 准备参数
       InfErrorCodePageReqVO reqVO = new InfErrorCodePageReqVO();
       reqVO.setType(InfErrorCodeTypeEnum.AUTO_GENERATION.getType());
       reqVO.setApplicationName("yudao");
       reqVO.setCode(1);
       reqVO.setMessage("yu");
       reqVO.setBeginCreateTime(buildTime(2020, 11, 1));
       reqVO.setEndCreateTime(buildTime(2020, 11, 30));

       // 调用
       PageResult<InfErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbErrorCode, pageResult.getList().get(0));
    }

    /**
     * 初始化 getErrorCodePage 方法的测试数据
     */
    private InfErrorCodeDO initGetErrorCodePage() {
        InfErrorCodeDO dbErrorCode = randomInfErrorCodeDO(o -> { // 等会查询到
            o.setType(InfErrorCodeTypeEnum.AUTO_GENERATION.getType());
            o.setApplicationName("yudaoyuanma");
            o.setCode(1);
            o.setMessage("yudao");
            o.setCreateTime(buildTime(2020, 11, 11));
        });
        errorCodeMapper.insert(dbErrorCode);
        // 测试 type 不匹配
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setType(InfErrorCodeTypeEnum.MANUAL_OPERATION.getType())));
        // 测试 applicationName 不匹配
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setApplicationName("yunai")));
        // 测试 code 不匹配
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setCode(2)));
        // 测试 message 不匹配
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setMessage("nai")));
        // 测试 createTime 不匹配
        errorCodeMapper.insert(ObjectUtils.clone(dbErrorCode, o -> o.setCreateTime(buildTime(2020, 12, 12))));
        return dbErrorCode;
    }

    @Test
    public void testGetErrorCodeList() {
        // mock 数据
        InfErrorCodeDO dbErrorCode = initGetErrorCodePage();
        // 准备参数
        InfErrorCodeExportReqVO reqVO = new InfErrorCodeExportReqVO();
        reqVO.setType(InfErrorCodeTypeEnum.AUTO_GENERATION.getType());
        reqVO.setApplicationName("yudao");
        reqVO.setCode(1);
        reqVO.setMessage("yu");
        reqVO.setBeginCreateTime(buildTime(2020, 11, 1));
        reqVO.setEndCreateTime(buildTime(2020, 11, 30));

        // 调用
        List<InfErrorCodeDO> list = errorCodeService.getErrorCodeList(reqVO);
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

    // ========== 随机对象 ==========

    @SafeVarargs
    private static InfErrorCodeDO randomInfErrorCodeDO(Consumer<InfErrorCodeDO>... consumers) {
        Consumer<InfErrorCodeDO> consumer = (o) -> {
            o.setType(randomEle(InfConfigTypeEnum.values()).getType()); // 保证 key 的范围
        };
        return randomPojo(InfErrorCodeDO.class, ArrayUtils.append(consumer, consumers));
    }

}
