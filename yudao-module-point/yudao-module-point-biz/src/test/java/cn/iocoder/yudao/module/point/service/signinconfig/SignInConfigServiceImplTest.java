package cn.iocoder.yudao.module.point.service.signinconfig;

import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinconfig.SignInConfigDO;
import cn.iocoder.yudao.module.point.dal.mysql.signinconfig.SignInConfigMapper;
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
 * {@link SignInConfigServiceImpl} 的单元测试类
 *
 * @author QingX
 */
@Import(SignInConfigServiceImpl.class)
public class SignInConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SignInConfigServiceImpl signInConfigService;

    @Resource
    private SignInConfigMapper signInConfigMapper;

    @Test
    public void testCreateSignInConfig_success() {
        // 准备参数
        SignInConfigCreateReqVO reqVO = randomPojo(SignInConfigCreateReqVO.class);

        // 调用
        Integer signInConfigId = signInConfigService.createSignInConfig(reqVO);
        // 断言
        assertNotNull(signInConfigId);
        // 校验记录的属性是否正确
        SignInConfigDO signInConfig = signInConfigMapper.selectById(signInConfigId);
        assertPojoEquals(reqVO, signInConfig);
    }

    @Test
    public void testUpdateSignInConfig_success() {
        // mock 数据
        SignInConfigDO dbSignInConfig = randomPojo(SignInConfigDO.class);
        signInConfigMapper.insert(dbSignInConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SignInConfigUpdateReqVO reqVO = randomPojo(SignInConfigUpdateReqVO.class, o -> {
            o.setId(dbSignInConfig.getId()); // 设置更新的 ID
        });

        // 调用
        signInConfigService.updateSignInConfig(reqVO);
        // 校验是否更新正确
        SignInConfigDO signInConfig = signInConfigMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, signInConfig);
    }

    @Test
    public void testUpdateSignInConfig_notExists() {
        // 准备参数
        SignInConfigUpdateReqVO reqVO = randomPojo(SignInConfigUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> signInConfigService.updateSignInConfig(reqVO), SIGN_IN_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteSignInConfig_success() {
        // mock 数据
        SignInConfigDO dbSignInConfig = randomPojo(SignInConfigDO.class);
        signInConfigMapper.insert(dbSignInConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbSignInConfig.getId();

        // 调用
        signInConfigService.deleteSignInConfig(id);
       // 校验数据不存在了
       assertNull(signInConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteSignInConfig_notExists() {
        // 准备参数
        Integer id = RandomUtils.randomInteger();

        // 调用, 并断言异常
        assertServiceException(() -> signInConfigService.deleteSignInConfig(id), SIGN_IN_CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSignInConfigPage() {
       // mock 数据
       SignInConfigDO dbSignInConfig = randomPojo(SignInConfigDO.class, o -> { // 等会查询到
           o.setDay(null);
       });
       signInConfigMapper.insert(dbSignInConfig);
       // 测试 day 不匹配
       signInConfigMapper.insert(cloneIgnoreId(dbSignInConfig, o -> o.setDay(null)));
       // 准备参数
       SignInConfigPageReqVO reqVO = new SignInConfigPageReqVO();
       reqVO.setDay(null);

       // 调用
       PageResult<SignInConfigDO> pageResult = signInConfigService.getSignInConfigPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSignInConfig, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSignInConfigList() {
       // mock 数据
       SignInConfigDO dbSignInConfig = randomPojo(SignInConfigDO.class, o -> { // 等会查询到
           o.setDay(null);
       });
       signInConfigMapper.insert(dbSignInConfig);
       // 测试 day 不匹配
       signInConfigMapper.insert(cloneIgnoreId(dbSignInConfig, o -> o.setDay(null)));
       // 准备参数
       SignInConfigExportReqVO reqVO = new SignInConfigExportReqVO();
       reqVO.setDay(null);

       // 调用
       List<SignInConfigDO> list = signInConfigService.getSignInConfigList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSignInConfig, list.get(0));
    }

}
