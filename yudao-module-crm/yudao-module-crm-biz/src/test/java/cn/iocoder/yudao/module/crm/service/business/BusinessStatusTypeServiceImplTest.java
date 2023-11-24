package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.BusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.BusinessStatusTypeSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.BusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.BusinessStatusTypeMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_TYPE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link BusinessStatusTypeServiceImpl} 的单元测试类
 *
 * @author ljlleo
 */
@Import(BusinessStatusTypeServiceImpl.class)
public class BusinessStatusTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private BusinessStatusTypeServiceImpl businessStatusTypeService;

    @Resource
    private BusinessStatusTypeMapper businessStatusTypeMapper;

    @Test
    public void testCreateBusinessStatusType_success() {
        // 准备参数
        BusinessStatusTypeSaveReqVO createReqVO = randomPojo(BusinessStatusTypeSaveReqVO.class).setId(null);

        // 调用
        Long businessStatusTypeId = businessStatusTypeService.createBusinessStatusType(createReqVO);
        // 断言
        assertNotNull(businessStatusTypeId);
        // 校验记录的属性是否正确
        BusinessStatusTypeDO businessStatusType = businessStatusTypeMapper.selectById(businessStatusTypeId);
        assertPojoEquals(createReqVO, businessStatusType, "id");
    }

    @Test
    public void testUpdateBusinessStatusType_success() {
        // mock 数据
        BusinessStatusTypeDO dbBusinessStatusType = randomPojo(BusinessStatusTypeDO.class);
        businessStatusTypeMapper.insert(dbBusinessStatusType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        BusinessStatusTypeSaveReqVO updateReqVO = randomPojo(BusinessStatusTypeSaveReqVO.class, o -> {
            o.setId(dbBusinessStatusType.getId()); // 设置更新的 ID
        });

        // 调用
        businessStatusTypeService.updateBusinessStatusType(updateReqVO);
        // 校验是否更新正确
        BusinessStatusTypeDO businessStatusType = businessStatusTypeMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, businessStatusType);
    }

    @Test
    public void testUpdateBusinessStatusType_notExists() {
        // 准备参数
        BusinessStatusTypeSaveReqVO updateReqVO = randomPojo(BusinessStatusTypeSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> businessStatusTypeService.updateBusinessStatusType(updateReqVO), BUSINESS_STATUS_TYPE_NOT_EXISTS);
    }

    @Test
    public void testDeleteBusinessStatusType_success() {
        // mock 数据
        BusinessStatusTypeDO dbBusinessStatusType = randomPojo(BusinessStatusTypeDO.class);
        businessStatusTypeMapper.insert(dbBusinessStatusType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbBusinessStatusType.getId();

        // 调用
        businessStatusTypeService.deleteBusinessStatusType(id);
       // 校验数据不存在了
       assertNull(businessStatusTypeMapper.selectById(id));
    }

    @Test
    public void testDeleteBusinessStatusType_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> businessStatusTypeService.deleteBusinessStatusType(id), BUSINESS_STATUS_TYPE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBusinessStatusTypePage() {
       // mock 数据
       BusinessStatusTypeDO dbBusinessStatusType = randomPojo(BusinessStatusTypeDO.class, o -> { // 等会查询到
       });
       businessStatusTypeMapper.insert(dbBusinessStatusType);
       // 准备参数
       BusinessStatusTypePageReqVO reqVO = new BusinessStatusTypePageReqVO();

       // 调用
       PageResult<BusinessStatusTypeDO> pageResult = businessStatusTypeService.getBusinessStatusTypePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbBusinessStatusType, pageResult.getList().get(0));
    }

}
