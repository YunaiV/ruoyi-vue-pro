package cn.iocoder.yudao.module.erp.service.finance;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import jakarta.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.*;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpAccountDO;
import cn.iocoder.yudao.module.erp.dal.mysql.finance.ErpAccountMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ErpAccountServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(ErpAccountServiceImpl.class)
public class ErpAccountServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ErpAccountServiceImpl accountService;

    @Resource
    private ErpAccountMapper accountMapper;

    @Test
    public void testCreateAccount_success() {
        // 准备参数
        ErpAccountSaveReqVO createReqVO = randomPojo(ErpAccountSaveReqVO.class).setId(null);

        // 调用
        Long accountId = accountService.createAccount(createReqVO);
        // 断言
        assertNotNull(accountId);
        // 校验记录的属性是否正确
        ErpAccountDO account = accountMapper.selectById(accountId);
        assertPojoEquals(createReqVO, account, "id");
    }

    @Test
    public void testUpdateAccount_success() {
        // mock 数据
        ErpAccountDO dbAccount = randomPojo(ErpAccountDO.class);
        accountMapper.insert(dbAccount);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ErpAccountSaveReqVO updateReqVO = randomPojo(ErpAccountSaveReqVO.class, o -> {
            o.setId(dbAccount.getId()); // 设置更新的 ID
        });

        // 调用
        accountService.updateAccount(updateReqVO);
        // 校验是否更新正确
        ErpAccountDO account = accountMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, account);
    }

    @Test
    public void testUpdateAccount_notExists() {
        // 准备参数
        ErpAccountSaveReqVO updateReqVO = randomPojo(ErpAccountSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> accountService.updateAccount(updateReqVO), ACCOUNT_NOT_EXISTS);
    }

    @Test
    public void testDeleteAccount_success() {
        // mock 数据
        ErpAccountDO dbAccount = randomPojo(ErpAccountDO.class);
        accountMapper.insert(dbAccount);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbAccount.getId();

        // 调用
        accountService.deleteAccount(id);
       // 校验数据不存在了
       assertNull(accountMapper.selectById(id));
    }

    @Test
    public void testDeleteAccount_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> accountService.deleteAccount(id), ACCOUNT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAccountPage() {
       // mock 数据
       ErpAccountDO dbAccount = randomPojo(ErpAccountDO.class, o -> { // 等会查询到
           o.setNo(null);
           o.setRemark(null);
           o.setStatus(null);
           o.setName(null);
       });
       accountMapper.insert(dbAccount);
       // 测试 no 不匹配
       accountMapper.insert(cloneIgnoreId(dbAccount, o -> o.setNo(null)));
       // 测试 remark 不匹配
       accountMapper.insert(cloneIgnoreId(dbAccount, o -> o.setRemark(null)));
       // 测试 status 不匹配
       accountMapper.insert(cloneIgnoreId(dbAccount, o -> o.setStatus(null)));
       // 测试 name 不匹配
       accountMapper.insert(cloneIgnoreId(dbAccount, o -> o.setName(null)));
       // 准备参数
       ErpAccountPageReqVO reqVO = new ErpAccountPageReqVO();
       reqVO.setNo(null);
       reqVO.setRemark(null);
       reqVO.setStatus(null);
       reqVO.setName(null);

       // 调用
       PageResult<ErpAccountDO> pageResult = accountService.getAccountPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbAccount, pageResult.getList().get(0));
    }

}