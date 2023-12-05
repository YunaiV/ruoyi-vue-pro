package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link CrmBusinessServiceImpl} 的单元测试类
 *
 * @author ljlleo
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(CrmBusinessServiceImpl.class)
public class CrmBusinessServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmBusinessServiceImpl businessService;

    @Resource
    private CrmBusinessMapper businessMapper;

    @Test
    public void testCreateBusiness_success() {
        // 准备参数
        CrmBusinessCreateReqVO reqVO = randomPojo(CrmBusinessCreateReqVO.class);

        // 调用
        Long businessId = businessService.createBusiness(reqVO, getLoginUserId());
        // 断言
        assertNotNull(businessId);
        // 校验记录的属性是否正确
        CrmBusinessDO business = businessMapper.selectById(businessId);
        assertPojoEquals(reqVO, business);
    }

    @Test
    public void testUpdateBusiness_success() {
        // mock 数据
        CrmBusinessDO dbBusiness = randomPojo(CrmBusinessDO.class);
        businessMapper.insert(dbBusiness);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmBusinessUpdateReqVO reqVO = randomPojo(CrmBusinessUpdateReqVO.class, o -> {
            o.setId(dbBusiness.getId()); // 设置更新的 ID
        });

        // 调用
        businessService.updateBusiness(reqVO);
        // 校验是否更新正确
        CrmBusinessDO business = businessMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, business);
    }

    @Test
    public void testUpdateBusiness_notExists() {
        // 准备参数
        CrmBusinessUpdateReqVO reqVO = randomPojo(CrmBusinessUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> businessService.updateBusiness(reqVO), BUSINESS_NOT_EXISTS);
    }

    @Test
    public void testDeleteBusiness_success() {
        // mock 数据
        CrmBusinessDO dbBusiness = randomPojo(CrmBusinessDO.class);
        businessMapper.insert(dbBusiness);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbBusiness.getId();

        // 调用
        businessService.deleteBusiness(id);
       // 校验数据不存在了
       assertNull(businessMapper.selectById(id));
    }

    @Test
    public void testDeleteBusiness_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> businessService.deleteBusiness(id), BUSINESS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBusinessPage() {
       // mock 数据
       CrmBusinessDO dbBusiness = randomPojo(CrmBusinessDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setStatusTypeId(null);
           o.setStatusId(null);
           o.setContactNextTime(null);
           o.setCustomerId(null);
           o.setDealTime(null);
           o.setPrice(null);
           o.setDiscountPercent(null);
           o.setProductPrice(null);
           o.setRemark(null);
           o.setCreateTime(null);
           o.setEndStatus(null);
           o.setEndRemark(null);
           o.setContactLastTime(null);
           o.setFollowUpStatus(null);
       });
       businessMapper.insert(dbBusiness);
       // 测试 name 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setName(null)));
       // 测试 statusTypeId 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setStatusTypeId(null)));
       // 测试 statusId 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setStatusId(null)));
       // 测试 contactNextTime 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setContactNextTime(null)));
       // 测试 customerId 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setCustomerId(null)));
       // 测试 dealTime 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setDealTime(null)));
       // 测试 price 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setPrice(null)));
       // 测试 discountPercent 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setDiscountPercent(null)));
       // 测试 productPrice 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setProductPrice(null)));
       // 测试 remark 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setCreateTime(null)));
       // 测试 endStatus 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setEndStatus(null)));
       // 测试 endRemark 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setEndRemark(null)));
       // 测试 contactLastTime 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setContactLastTime(null)));
       // 测试 followUpStatus 不匹配
       businessMapper.insert(cloneIgnoreId(dbBusiness, o -> o.setFollowUpStatus(null)));
        //// 准备参数
        //CrmBusinessPageReqVO reqVO = new CrmBusinessPageReqVO();
        //reqVO.setName(null);
        //reqVO.setStatusTypeId(null);
        //reqVO.setStatusId(null);
        //reqVO.setContactNextTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
        //reqVO.setCustomerId(null);
        //reqVO.setDealTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
        //reqVO.setPrice(null);
        //reqVO.setDiscountPercent(null);
        //reqVO.setProductPrice(null);
        //reqVO.setRemark(null);
        //reqVO.setOwnerUserId(null);
        //reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
        //reqVO.setRoUserIds(null);
        //reqVO.setRwUserIds(null);
        //reqVO.setEndStatus(null);
        //reqVO.setEndRemark(null);
        //reqVO.setContactLastTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
        //reqVO.setFollowUpStatus(null);
        //
        //// 调用
        //PageResult<CrmBusinessDO> pageResult = businessService.getBusinessPage(reqVO);
        //// 断言
        //assertEquals(1, pageResult.getTotal());
        //assertEquals(1, pageResult.getList().size());
        //assertPojoEquals(dbBusiness, pageResult.getList().get(0));
    }

}
