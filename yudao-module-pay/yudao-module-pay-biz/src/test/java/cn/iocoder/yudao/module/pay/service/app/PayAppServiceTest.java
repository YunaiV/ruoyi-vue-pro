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
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_APP_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@Import(PayAppServiceImpl.class)
public class PayAppServiceTest extends BaseDbUnitTest {

    @Resource
    private PayAppServiceImpl appService;

    @Resource
    private PayAppMapper appMapper;

    @Test
    public void testCreateApp_success() {
        // 准备参数
        PayAppCreateReqVO reqVO = randomPojo(PayAppCreateReqVO.class, o ->
                o.setStatus((RandomUtil.randomEle(CommonStatusEnum.values()).getStatus())));

        // 调用
        Long appId = appService.createApp(reqVO);
        // 断言
        assertNotNull(appId);
        // 校验记录的属性是否正确
        PayAppDO app = appMapper.selectById(appId);
        assertPojoEquals(reqVO, app);
    }

    @Test
    public void testUpdateApp_success() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class, o ->
                o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PayAppUpdateReqVO reqVO = randomPojo(PayAppUpdateReqVO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
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
        assertServiceException(() -> appService.updateApp(reqVO), PAY_APP_NOT_FOUND);
    }

    @Test
    public void testDeleteApp_success() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class, o ->
                o.setStatus((RandomUtil.randomEle(CommonStatusEnum.values()).getStatus())));
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
        assertServiceException(() -> appService.deleteApp(id), PAY_APP_NOT_FOUND);
    }

    @Test
    public void testGetAppPage() {
        // mock 数据
        PayAppDO dbApp = randomPojo(PayAppDO.class, o -> { // 等会查询到
            o.setName("灿灿姐的杂货铺");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setRemark("敏敏姐的小卖铺");
            o.setPayNotifyUrl("https://www.hc.com");
            o.setRefundNotifyUrl("https://www.xm.com");
            o.setCreateTime(buildTime(2021,11,20));
        });

        appMapper.insert(dbApp);
        // 测试 name 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setName("敏敏姐的杂货铺")));
        // 测试 status 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 remark 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setRemark("灿灿姐的小卖部")));
        // 测试 payNotifyUrl 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setPayNotifyUrl("xm.com")));
        // 测试 refundNotifyUrl 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setRefundNotifyUrl("hc.com")));
        // 测试 createTime 不匹配
        appMapper.insert(cloneIgnoreId(dbApp, o -> o.setCreateTime(buildTime(2021,12,21))));
        // 准备参数
        PayAppPageReqVO reqVO = new PayAppPageReqVO();
        reqVO.setName("灿灿姐的杂货铺");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setRemark("敏敏姐的小卖铺");
        reqVO.setPayNotifyUrl("https://www.hc.com");
        reqVO.setRefundNotifyUrl("https://www.xm.com");
        reqVO.setCreateTime((new LocalDateTime[]{buildTime(2021,11,19),buildTime(2021,11,21)}));

        // 调用
        PageResult<PayAppDO> pageResult = appService.getAppPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbApp, pageResult.getList().get(0));
    }

}
