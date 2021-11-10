package cn.iocoder.yudao.adminserver.modules.pay.app.service;

import cn.iocoder.yudao.adminserver.BaseDbUnitTest;
import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.PayAppCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.PayAppExportReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.PayAppPageReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.PayAppUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.pay.dal.mysql.app.PayAppMapper;
import cn.iocoder.yudao.adminserver.modules.pay.service.app.impl.PayAppServiceImpl;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.coreservice.modules.pay.enums.PayErrorCodeCoreConstants.APP_NOT_EXISTS;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link PayAppServiceImpl} 的单元测试类
*
* @author 芋艿
*/
@Import(PayAppServiceImpl.class)
public class PayAppServiceTest extends BaseDbUnitTest {

    @Resource
    private PayAppServiceImpl appService;

    @Resource
    private PayAppMapper appMapper;

    @Test
    public void testCreateApp_success() {
        // 准备参数
        PayAppCreateReqVO reqVO = randomPojo(PayAppCreateReqVO.class);

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
        PayAppDO dbApp = randomPojo(PayAppDO.class);
        appMapper.insert(dbApp);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PayAppUpdateReqVO reqVO = randomPojo(PayAppUpdateReqVO.class, o -> {
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
        PayAppUpdateReqVO reqVO = randomPojo(PayAppUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> appService.updateApp(reqVO), APP_NOT_EXISTS);
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
        assertServiceException(() -> appService.deleteApp(id), APP_NOT_EXISTS);
    }

    @Test // TODO 请修改 null 为需要的值
    public void testGetAppPage() {
       // mock 数据
       PayAppDO dbApp = randomPojo(PayAppDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setPayNotifyUrl(null);
           o.setRefundNotifyUrl(null);
           o.setMerchantId(null);
           o.setCreateTime(null);
       });
       appMapper.insert(dbApp);
       // 测试 name 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setName(null)));
       // 测试 status 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setRemark(null)));
       // 测试 payNotifyUrl 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setPayNotifyUrl(null)));
       // 测试 refundNotifyUrl 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setRefundNotifyUrl(null)));
       // 测试 merchantId 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setMerchantId(null)));
       // 测试 createTime 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setCreateTime(null)));
       // 准备参数
       PayAppPageReqVO reqVO = new PayAppPageReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setPayNotifyUrl(null);
       reqVO.setRefundNotifyUrl(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       PageResult<PayAppDO> pageResult = appService.getAppPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbApp, pageResult.getList().get(0));
    }

    @Test // TODO aquan：请修改 null 为需要的值
    public void testGetAppList() {
       // mock 数据
       PayAppDO dbApp = randomPojo(PayAppDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setPayNotifyUrl(null);
           o.setRefundNotifyUrl(null);
           o.setMerchantId(null);
           o.setCreateTime(null);
       });
       appMapper.insert(dbApp);
       // 测试 name 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setName(null)));
       // 测试 status 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setRemark(null)));
       // 测试 payNotifyUrl 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setPayNotifyUrl(null)));
       // 测试 refundNotifyUrl 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setRefundNotifyUrl(null)));
       // 测试 merchantId 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setMerchantId(null)));
       // 测试 createTime 不匹配
       appMapper.insert(ObjectUtils.clone(dbApp, o -> o.setCreateTime(null)));
       // 准备参数
       PayAppExportReqVO reqVO = new PayAppExportReqVO();
       reqVO.setName(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setPayNotifyUrl(null);
       reqVO.setRefundNotifyUrl(null);
       reqVO.setMerchantId(null);
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);

       // 调用
       List<PayAppDO> list = appService.getAppList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbApp, list.get(0));
    }

}
