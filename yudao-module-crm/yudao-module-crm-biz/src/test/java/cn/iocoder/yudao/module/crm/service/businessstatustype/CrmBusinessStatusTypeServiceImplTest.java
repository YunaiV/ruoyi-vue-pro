package cn.iocoder.yudao.module.crm.service.businessstatustype;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.mysql.businessstatustype.CrmBusinessStatusTypeMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_TYPE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CrmBusinessStatusTypeServiceImpl} 的单元测试类
 *
 * @author ljlleo
 */
@Import(CrmBusinessStatusTypeServiceImpl.class)
public class CrmBusinessStatusTypeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmBusinessStatusTypeServiceImpl businessStatusTypeService;

    @Resource
    private CrmBusinessStatusTypeMapper businessStatusTypeMapper;

    @Test
    public void testCreateBusinessStatusType_success() {
        // 准备参数
        CrmBusinessStatusTypeCreateReqVO reqVO = randomPojo(CrmBusinessStatusTypeCreateReqVO.class);

        // 调用
        Long businessStatusTypeId = businessStatusTypeService.createBusinessStatusType(reqVO);
        // 断言
        assertNotNull(businessStatusTypeId);
        // 校验记录的属性是否正确
        CrmBusinessStatusTypeDO businessStatusType = businessStatusTypeMapper.selectById(businessStatusTypeId);
        assertPojoEquals(reqVO, businessStatusType);
    }

    @Test
    public void testUpdateBusinessStatusType_success() {
        // mock 数据
        CrmBusinessStatusTypeDO dbBusinessStatusType = randomPojo(CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.insert(dbBusinessStatusType);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmBusinessStatusTypeUpdateReqVO reqVO = randomPojo(CrmBusinessStatusTypeUpdateReqVO.class, o -> {
            o.setId(dbBusinessStatusType.getId()); // 设置更新的 ID
        });

        // 调用
        businessStatusTypeService.updateBusinessStatusType(reqVO);
        // 校验是否更新正确
        CrmBusinessStatusTypeDO businessStatusType = businessStatusTypeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, businessStatusType);
    }

    @Test
    public void testUpdateBusinessStatusType_notExists() {
        // 准备参数
        CrmBusinessStatusTypeUpdateReqVO reqVO = randomPojo(CrmBusinessStatusTypeUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> businessStatusTypeService.updateBusinessStatusType(reqVO), BUSINESS_STATUS_TYPE_NOT_EXISTS);
    }

    @Test
    public void testDeleteBusinessStatusType_success() {
        // mock 数据
        CrmBusinessStatusTypeDO dbBusinessStatusType = randomPojo(CrmBusinessStatusTypeDO.class);
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
       CrmBusinessStatusTypeDO dbBusinessStatusType = randomPojo(CrmBusinessStatusTypeDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setDeptIds(null);
           o.setStatus(null);
           //o.setCreateTime(null);
       });
       businessStatusTypeMapper.insert(dbBusinessStatusType);
       // 测试 name 不匹配
       businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setName(null)));
       // 测试 deptIds 不匹配
       businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setDeptIds(null)));
       // 测试 status 不匹配
       businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
        //businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setCreateTime(null)));
       // 准备参数
       CrmBusinessStatusTypePageReqVO reqVO = new CrmBusinessStatusTypePageReqVO();
       reqVO.setName(null);
        //reqVO.setDeptIds(null);
       reqVO.setStatus(null);
        //reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<CrmBusinessStatusTypeDO> pageResult = businessStatusTypeService.getBusinessStatusTypePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbBusinessStatusType, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBusinessStatusTypeList() {
       // mock 数据
       CrmBusinessStatusTypeDO dbBusinessStatusType = randomPojo(CrmBusinessStatusTypeDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setDeptIds(null);
           o.setStatus(null);
           //o.setCreateTime(null);
       });
       businessStatusTypeMapper.insert(dbBusinessStatusType);
       // 测试 name 不匹配
       businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setName(null)));
       // 测试 deptIds 不匹配
       businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setDeptIds(null)));
       // 测试 status 不匹配
       businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setStatus(null)));
       // 测试 createTime 不匹配
        //businessStatusTypeMapper.insert(cloneIgnoreId(dbBusinessStatusType, o -> o.setCreateTime(null)));
       // 准备参数
       CrmBusinessStatusTypeExportReqVO reqVO = new CrmBusinessStatusTypeExportReqVO();
       reqVO.setName(null);
       reqVO.setDeptIds(null);
       reqVO.setStatus(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.getBusinessStatusTypeList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbBusinessStatusType, list.get(0));
    }

}
