package cn.iocoder.yudao.module.pay.service.app;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.app.vo.PayAppUpdateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.mysql.app.PayAppMapper;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.refund.PayRefundService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link PayAppServiceImpl} 的单元测试
 *
 * @author aquan
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(PayAppServiceImpl.class)
public class PayAppServiceTest extends BaseDbUnitTest {

    @Resource
    private PayAppServiceImpl appService;

    @Resource
    private PayAppMapper appMapper;

    @MockBean
    private PayOrderService orderService;
    @MockBean
    private PayRefundService refundService;

    @Test
    public void testCreateApp_success() {
        // 准备参数
        PayAppCreateReqVO reqVO = randomPojo(PayAppCreateReqVO.class, o ->
                o.setStatus((RandomUtil.randomEle(CommonStatusEnum.values()).getStatus()))
                        .setOrderNotifyUrl(randomURL())
                        .setRefundNotifyUrl(randomURL()));

        // 调用
        Long appId = appService.createApp(reqVO);
        // 断言
        assertNotNull(appId);
        PayAppDO app = appMapper.selectById(appId);
        assertPojoEquals(reqVO, app);
    }

    @Test
    public void testUpdateApp_success() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PayAppUpdateReqVO reqVO = randomPojo(PayAppUpdateReqVO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setOrderNotifyUrl(randomURL()).setRefundNotifyUrl(randomURL());
            o.setId(dbApp.getId()); // 设置更新的 ID
        });

        // 调用
        appService.updateApp(reqVO);
        // 校验是否更新正确
        PayAppDO app = appMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, app);
    }

    @Test
    public void testUpdateApp_notExists() {
        // 准备参数
        PayAppUpdateReqVO reqVO = randomPojo(PayAppUpdateReqVO.class, o ->
                o.setStatus((RandomUtil.randomEle(CommonStatusEnum.values()).getStatus())));
        // 调用, 并断言异常
        assertServiceException(() -> appService.updateApp(reqVO), APP_NOT_FOUND);
    }

    @Test
    public void testUpdateAppStatus() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class, o ->
                o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据

        // 准备参数
        Long id = dbApp.getId();
        Integer status = CommonStatusEnum.ENABLE.getStatus();
        // 调用
        appService.updateAppStatus(id, status);
        // 断言
        PayAppDO app = appMapper.selectById(id); // 获取最新的
        assertEquals(status, app.getStatus());
    }

    @Test
    public void testDeleteApp_success() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp.getId();

        // 调用
        appService.deleteApp(id);
        // 校验数据不存在了
        assertNull(appMapper.selectById(id));
    }

    @Test
    public void testDeleteApp_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> appService.deleteApp(id), APP_NOT_FOUND);
    }

    @Test
    public void testDeleteApp_existOrder() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp.getId();
        // mock 订单有订单
        when(orderService.getOrderCountByAppId(eq(id))).thenReturn(10L);

        // 调用, 并断言异常
        assertServiceException(() -> appService.deleteApp(id), APP_EXIST_ORDER_CANT_DELETE);
    }

    @Test
    public void testDeleteApp_existRefund() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp.getId();
        // mock 订单有订单
        when(refundService.getRefundCountByAppId(eq(id))).thenReturn(10L);

        // 调用, 并断言异常
        assertServiceException(() -> appService.deleteApp(id), APP_EXIST_REFUND_CANT_DELETE);
    }

    @Test
    public void testApp() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp.getId();

        // 调用
        PayAppDO app = appService.getApp(id);
        // 校验数据一致
        assertPojoEquals(app, dbApp);
    }

    @Test
    public void testAppMap() {
        // mock 数据
        PayAppDO dbApp01 = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp01);// @Sql: 先插入出一条存在的数据
        PayAppDO dbApp02 = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp02);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp01.getId();

        // 调用
        Map<Long, PayAppDO> appMap = appService.getAppMap(singleton(id));
        // 校验数据一致
        assertEquals(1, appMap.size());
        assertPojoEquals(dbApp01, appMap.get(id));
    }

    @Test
    public void testGetAppPage() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class, o -> { // 等会查询到
            o.setName("灿灿姐的杂货铺");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2021,11,20));
        });

        appMapper.insert(dbApp);
        // 测试 name 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setName("敏敏姐的杂货铺")));
        // 测试 status 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 createTime 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setCreateTime(buildTime(2021,12,21))));
        // 准备参数
        PayAppPageReqVO reqVO = new PayAppPageReqVO();
        reqVO.setName("灿灿姐的杂货铺");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(buildBetweenTime(2021, 11, 19, 2021, 11, 21));

        // 调用
        PageResult<PayAppDO> pageResult = appService.getAppPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbApp, pageResult.getList().get(0));
    }

    @Test
    public void testValidPayApp_success() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class,
                o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp.getId();

        // 调用
        PayAppDO app = appService.validPayApp(id);
        // 校验数据一致
        assertPojoEquals(app, dbApp);
    }

    @Test
    public void testValidPayApp_notFound() {
        assertServiceException(() -> appService.validPayApp(randomLongId()), APP_NOT_FOUND);
    }

    @Test
    public void testValidPayApp_disable() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class,
                o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbApp.getId();

        // 调用，并断言异常
        assertServiceException(() -> appService.validPayApp(id), APP_IS_DISABLE);
    }

}
