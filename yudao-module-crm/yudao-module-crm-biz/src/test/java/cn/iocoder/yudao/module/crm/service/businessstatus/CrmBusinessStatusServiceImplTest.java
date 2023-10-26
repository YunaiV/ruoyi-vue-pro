package cn.iocoder.yudao.module.crm.service.businessstatus;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatus.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.mysql.businessstatus.CrmBusinessStatusMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CrmBusinessStatusServiceImpl} 的单元测试类
 *
 * @author ljlleo
 */
@Import(CrmBusinessStatusServiceImpl.class)
public class CrmBusinessStatusServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmBusinessStatusServiceImpl businessStatusService;

    @Resource
    private CrmBusinessStatusMapper businessStatusMapper;

    @Test
    public void testCreateBusinessStatus_success() {
        // 准备参数
        CrmBusinessStatusCreateReqVO reqVO = randomPojo(CrmBusinessStatusCreateReqVO.class);

        // 调用
        Long businessStatusId = businessStatusService.createBusinessStatus(reqVO);
        // 断言
        assertNotNull(businessStatusId);
        // 校验记录的属性是否正确
        CrmBusinessStatusDO businessStatus = businessStatusMapper.selectById(businessStatusId);
        assertPojoEquals(reqVO, businessStatus);
    }

    @Test
    public void testUpdateBusinessStatus_success() {
        // mock 数据
        CrmBusinessStatusDO dbBusinessStatus = randomPojo(CrmBusinessStatusDO.class);
        businessStatusMapper.insert(dbBusinessStatus);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmBusinessStatusUpdateReqVO reqVO = randomPojo(CrmBusinessStatusUpdateReqVO.class, o -> {
            o.setId(dbBusinessStatus.getId()); // 设置更新的 ID
        });

        // 调用
        businessStatusService.updateBusinessStatus(reqVO);
        // 校验是否更新正确
        CrmBusinessStatusDO businessStatus = businessStatusMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, businessStatus);
    }

    @Test
    public void testUpdateBusinessStatus_notExists() {
        // 准备参数
        CrmBusinessStatusUpdateReqVO reqVO = randomPojo(CrmBusinessStatusUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> businessStatusService.updateBusinessStatus(reqVO), BUSINESS_STATUS_NOT_EXISTS);
    }

    @Test
    public void testDeleteBusinessStatus_success() {
        // mock 数据
        CrmBusinessStatusDO dbBusinessStatus = randomPojo(CrmBusinessStatusDO.class);
        businessStatusMapper.insert(dbBusinessStatus);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbBusinessStatus.getId();

        // 调用
        businessStatusService.deleteBusinessStatus(id);
       // 校验数据不存在了
       assertNull(businessStatusMapper.selectById(id));
    }

    @Test
    public void testDeleteBusinessStatus_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> businessStatusService.deleteBusinessStatus(id), BUSINESS_STATUS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBusinessStatusPage() {
       // mock 数据
       CrmBusinessStatusDO dbBusinessStatus = randomPojo(CrmBusinessStatusDO.class, o -> { // 等会查询到
           o.setTypeId(null);
           o.setName(null);
           o.setPercent(null);
           o.setSort(null);
       });
       businessStatusMapper.insert(dbBusinessStatus);
       // 测试 typeId 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setTypeId(null)));
       // 测试 name 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setName(null)));
       // 测试 percent 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setPercent(null)));
       // 测试 sort 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setSort(null)));
       // 准备参数
       CrmBusinessStatusPageReqVO reqVO = new CrmBusinessStatusPageReqVO();
       reqVO.setTypeId(null);
       reqVO.setName(null);
       reqVO.setPercent(null);
       reqVO.setSort(null);

       // 调用
       PageResult<CrmBusinessStatusDO> pageResult = businessStatusService.getBusinessStatusPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbBusinessStatus, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetBusinessStatusList() {
       // mock 数据
       CrmBusinessStatusDO dbBusinessStatus = randomPojo(CrmBusinessStatusDO.class, o -> { // 等会查询到
           o.setTypeId(null);
           o.setName(null);
           o.setPercent(null);
           o.setSort(null);
       });
       businessStatusMapper.insert(dbBusinessStatus);
       // 测试 typeId 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setTypeId(null)));
       // 测试 name 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setName(null)));
       // 测试 percent 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setPercent(null)));
       // 测试 sort 不匹配
       businessStatusMapper.insert(cloneIgnoreId(dbBusinessStatus, o -> o.setSort(null)));
       // 准备参数
       CrmBusinessStatusExportReqVO reqVO = new CrmBusinessStatusExportReqVO();
       reqVO.setTypeId(null);
       reqVO.setName(null);
       reqVO.setPercent(null);
       reqVO.setSort(null);

       // 调用
       List<CrmBusinessStatusDO> list = businessStatusService.getBusinessStatusList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbBusinessStatus, list.get(0));
    }

}
