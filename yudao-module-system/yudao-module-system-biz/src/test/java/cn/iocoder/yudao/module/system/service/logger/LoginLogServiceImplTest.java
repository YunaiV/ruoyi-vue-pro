package cn.iocoder.yudao.module.system.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.LoginLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.LoginLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum.CAPTCHA_CODE_ERROR;
import static cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(LoginLogServiceImpl.class)
public class LoginLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private LoginLogServiceImpl loginLogService;

    @Resource
    private LoginLogMapper loginLogMapper;

    @Test
    public void testGetLoginLogPage() {
        // mock 数据
        LoginLogDO loginLogDO = randomPojo(LoginLogDO.class, o -> {
            o.setUserIp("192.168.199.16");
            o.setUsername("wang");
            o.setResult(SUCCESS.getResult());
            o.setCreateTime(buildTime(2021, 3, 6));
        });
        loginLogMapper.insert(loginLogDO);
        // 测试 status 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setResult(CAPTCHA_CODE_ERROR.getResult())));
        // 测试 ip 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUserIp("192.168.128.18")));
        // 测试 username 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUsername("yunai")));
        // 测试 createTime 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setCreateTime(buildTime(2021, 2, 6))));
        // 构造调用参数
        LoginLogPageReqVO reqVO = new LoginLogPageReqVO();
        reqVO.setUsername("wang");
        reqVO.setUserIp("192.168.199");
        reqVO.setStatus(true);
        reqVO.setCreateTime(buildBetweenTime(2021, 3, 5, 2021, 3, 7));

        // 调用
        PageResult<LoginLogDO> pageResult = loginLogService.getLoginLogPage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(loginLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetLoginLogList() {
        // mock 数据
        LoginLogDO loginLogDO = randomPojo(LoginLogDO.class, o -> {
            o.setUserIp("192.168.199.16");
            o.setUsername("wang");
            o.setResult(SUCCESS.getResult());
            o.setCreateTime(buildTime(2021, 3, 6));
        });
        loginLogMapper.insert(loginLogDO);
        // 测试 status 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setResult(CAPTCHA_CODE_ERROR.getResult())));
        // 测试 ip 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUserIp("192.168.128.18")));
        // 测试 username 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setUsername("yunai")));
        // 测试 createTime 不匹配
        loginLogMapper.insert(cloneIgnoreId(loginLogDO, o -> o.setCreateTime(buildTime(2021, 2, 6))));
        // 构造调用参数
        LoginLogExportReqVO reqVO = new LoginLogExportReqVO();
        reqVO.setUsername("wang");
        reqVO.setUserIp("192.168.199");
        reqVO.setStatus(true);
        reqVO.setCreateTime(buildBetweenTime(2021, 3, 5, 2021, 3, 7));

        // 调用service方法
        List<LoginLogDO> list = loginLogService.getLoginLogList(reqVO);
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(loginLogDO, list.get(0));
    }

    @Test
    public void testCreateLoginLog() {
        LoginLogCreateReqDTO reqDTO = randomPojo(LoginLogCreateReqDTO.class);

        // 调用
        loginLogService.createLoginLog(reqDTO);
        // 断言
        LoginLogDO loginLogDO = loginLogMapper.selectOne(null);
        assertPojoEquals(reqDTO, loginLogDO);
    }

}
