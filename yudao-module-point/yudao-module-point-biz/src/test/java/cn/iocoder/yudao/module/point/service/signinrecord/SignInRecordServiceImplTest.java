package cn.iocoder.yudao.module.point.service.signinrecord;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinrecord.SignInRecordDO;
import cn.iocoder.yudao.module.point.dal.mysql.signinrecord.SignInRecordMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link SignInRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(SignInRecordServiceImpl.class)
public class SignInRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SignInRecordServiceImpl signInRecordService;

    @Resource
    private SignInRecordMapper signInRecordMapper;

    @Test
    public void testCreateSignInRecord_success() {
        // 准备参数
        SignInRecordCreateReqVO reqVO = randomPojo(SignInRecordCreateReqVO.class);

        // 调用
        Long signInRecordId = signInRecordService.createSignInRecord(reqVO);
        // 断言
        assertNotNull(signInRecordId);
        // 校验记录的属性是否正确
        SignInRecordDO signInRecord = signInRecordMapper.selectById(signInRecordId);
        assertPojoEquals(reqVO, signInRecord);
    }

    @Test
    public void testUpdateSignInRecord_success() {
        // mock 数据
        SignInRecordDO dbSignInRecord = randomPojo(SignInRecordDO.class);
        signInRecordMapper.insert(dbSignInRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SignInRecordUpdateReqVO reqVO = randomPojo(SignInRecordUpdateReqVO.class, o -> {
            o.setId(dbSignInRecord.getId()); // 设置更新的 ID
        });

        // 调用
        signInRecordService.updateSignInRecord(reqVO);
        // 校验是否更新正确
        SignInRecordDO signInRecord = signInRecordMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, signInRecord);
    }

    @Test
    public void testUpdateSignInRecord_notExists() {
        // 准备参数
        SignInRecordUpdateReqVO reqVO = randomPojo(SignInRecordUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> signInRecordService.updateSignInRecord(reqVO), SIGN_IN_RECORD_NOT_EXISTS);
    }

    @Test
    public void testDeleteSignInRecord_success() {
        // mock 数据
        SignInRecordDO dbSignInRecord = randomPojo(SignInRecordDO.class);
        signInRecordMapper.insert(dbSignInRecord);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSignInRecord.getId();

        // 调用
        signInRecordService.deleteSignInRecord(id);
       // 校验数据不存在了
       assertNull(signInRecordMapper.selectById(id));
    }

    @Test
    public void testDeleteSignInRecord_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> signInRecordService.deleteSignInRecord(id), SIGN_IN_RECORD_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSignInRecordPage() {
       // mock 数据
       SignInRecordDO dbSignInRecord = randomPojo(SignInRecordDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setDay(null);
           o.setCreateTime(null);
       });
       signInRecordMapper.insert(dbSignInRecord);
       // 测试 userId 不匹配
       signInRecordMapper.insert(cloneIgnoreId(dbSignInRecord, o -> o.setUserId(null)));
       // 测试 day 不匹配
       signInRecordMapper.insert(cloneIgnoreId(dbSignInRecord, o -> o.setDay(null)));
       // 测试 createTime 不匹配
       signInRecordMapper.insert(cloneIgnoreId(dbSignInRecord, o -> o.setCreateTime(null)));
       // 准备参数
       SignInRecordPageReqVO reqVO = new SignInRecordPageReqVO();
       reqVO.setUserId(null);
       reqVO.setDay(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SignInRecordDO> pageResult = signInRecordService.getSignInRecordPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSignInRecord, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSignInRecordList() {
       // mock 数据
       SignInRecordDO dbSignInRecord = randomPojo(SignInRecordDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setDay(null);
           o.setCreateTime(null);
       });
       signInRecordMapper.insert(dbSignInRecord);
       // 测试 userId 不匹配
       signInRecordMapper.insert(cloneIgnoreId(dbSignInRecord, o -> o.setUserId(null)));
       // 测试 day 不匹配
       signInRecordMapper.insert(cloneIgnoreId(dbSignInRecord, o -> o.setDay(null)));
       // 测试 createTime 不匹配
       signInRecordMapper.insert(cloneIgnoreId(dbSignInRecord, o -> o.setCreateTime(null)));
       // 准备参数
       SignInRecordExportReqVO reqVO = new SignInRecordExportReqVO();
       reqVO.setUserId(null);
       reqVO.setDay(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<SignInRecordDO> list = signInRecordService.getSignInRecordList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSignInRecord, list.get(0));
    }

}
