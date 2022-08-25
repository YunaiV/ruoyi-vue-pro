package cn.iocoder.yudao.module.pay.service.merchant;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant.PayMerchantCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant.PayMerchantExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant.PayMerchantPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant.PayMerchantUpdateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.dal.mysql.merchant.PayMerchantMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.PAY_MERCHANT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
* {@link PayMerchantServiceImpl} 的单元测试类
*
* @author aquan
*/
@Import(PayMerchantServiceImpl.class)
public class PayMerchantServiceTest extends BaseDbUnitTest {

    @Resource
    private PayMerchantServiceImpl merchantService;

    @Resource
    private PayMerchantMapper merchantMapper;

    @Test
    public void testCreateMerchant_success() {
        // 准备参数
        PayMerchantCreateReqVO reqVO = randomPojo(PayMerchantCreateReqVO.class,o ->
            o.setStatus(RandomUtil.randomEle(CommonStatusEnum.values()).getStatus()));

        // 调用
        Long merchantId = merchantService.createMerchant(reqVO);
        // 断言
        assertNotNull(merchantId);
        // 校验记录的属性是否正确
        PayMerchantDO merchant = merchantMapper.selectById(merchantId);
        assertPojoEquals(reqVO, merchant);
    }

    @Test
    public void testUpdateMerchant_success() {
        // mock 数据
        PayMerchantDO dbMerchant = randomPojo(PayMerchantDO.class, o ->
            o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        merchantMapper.insert(dbMerchant);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PayMerchantUpdateReqVO reqVO = randomPojo(PayMerchantUpdateReqVO.class, o -> {
            o.setId(dbMerchant.getId()); // 设置更新的 ID
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
        });

        // 调用
        merchantService.updateMerchant(reqVO);
        // 校验是否更新正确
        PayMerchantDO merchant = merchantMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, merchant);
    }

    @Test
    public void testUpdateMerchant_notExists() {
        // 准备参数
        PayMerchantUpdateReqVO reqVO = randomPojo(PayMerchantUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> merchantService.updateMerchant(reqVO), PAY_MERCHANT_NOT_EXISTS);
    }

    @Test
    public void testDeleteMerchant_success() {
        // mock 数据
        PayMerchantDO dbMerchant = randomPojo(PayMerchantDO.class,
                o-> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        merchantMapper.insert(dbMerchant);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbMerchant.getId();

        // 调用
        merchantService.deleteMerchant(id);
       // 校验数据不存在了
       assertNull(merchantMapper.selectById(id));
    }

    @Test
    public void testDeleteMerchant_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> merchantService.deleteMerchant(id), PAY_MERCHANT_NOT_EXISTS);
    }

    @Test
    public void testGetMerchantPage() {
       // mock 数据
       PayMerchantDO dbMerchant = randomPojo(PayMerchantDO.class, o -> { // 等会查询到
           o.setNo("M1008611");
           o.setName("灿哥的杂货铺");
           o.setShortName("灿灿子");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setRemark("灿哥的杂货铺");
           o.setCreateTime(buildTime(2021,11,3));
       });
       merchantMapper.insert(dbMerchant);
       // 测试 no 不匹配
       merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setNo("M200000")));
       // 测试 name 不匹配
       merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setName("斌哥的杂货铺")));
       // 测试 shortName 不匹配
       merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setShortName("斌斌子")));
       // 测试 status 不匹配
       merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
       // 测试 remark 不匹配
       merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setRemark("斌哥的杂货铺")));
       // 测试 createTime 不匹配
       merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setCreateTime(buildTime(2022,12,4))));
       // 准备参数
       PayMerchantPageReqVO reqVO = new PayMerchantPageReqVO();
       reqVO.setNo("M1008611");
       reqVO.setName("灿哥的杂货铺");
       reqVO.setShortName("灿灿子");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setRemark("灿哥的杂货铺");
       reqVO.setCreateTime((new Date[]{buildTime(2021,11,2),buildTime(2021,11,4)}));

       // 调用
       PageResult<PayMerchantDO> pageResult = merchantService.getMerchantPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbMerchant, pageResult.getList().get(0));
    }

    @Test
    public void testGetMerchantList() {
       // mock 数据
       PayMerchantDO dbMerchant = randomPojo(PayMerchantDO.class, o -> { // 等会查询到
           o.setNo("M1008611");
           o.setName("灿哥的杂货铺");
           o.setShortName("灿灿子");
           o.setStatus(CommonStatusEnum.ENABLE.getStatus());
           o.setRemark("灿哥的杂货铺");
           o.setCreateTime(buildTime(2021,11,3));
       });
        merchantMapper.insert(dbMerchant);
        // 测试 no 不匹配
        merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setNo("M200000")));
        // 测试 name 不匹配
        merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setName("斌哥的杂货铺")));
        // 测试 shortName 不匹配
        merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setShortName("斌斌子")));
        // 测试 status 不匹配
        merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 测试 remark 不匹配
        merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setRemark("斌哥的杂货铺")));
        // 测试 createTime 不匹配
        merchantMapper.insert(cloneIgnoreId(dbMerchant, o -> o.setCreateTime(buildTime(2022,12,4))));
       // 准备参数
       PayMerchantExportReqVO reqVO = new PayMerchantExportReqVO();
       reqVO.setNo("M1008611");
       reqVO.setName("灿哥的杂货铺");
       reqVO.setShortName("灿灿子");
       reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
       reqVO.setRemark("灿哥的杂货铺");
       reqVO.setCreateTime((new Date[]{buildTime(2021,11,2),buildTime(2021,11,4)}));

       // 调用
       List<PayMerchantDO> list = merchantService.getMerchantList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbMerchant, list.get(0));
    }

}
