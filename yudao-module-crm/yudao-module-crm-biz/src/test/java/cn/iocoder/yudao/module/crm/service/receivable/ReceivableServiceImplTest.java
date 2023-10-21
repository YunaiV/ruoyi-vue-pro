package cn.iocoder.yudao.module.crm.service.receivable;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.ReceivableMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ReceivableServiceImpl} 的单元测试类
 *
 * @author 赤焰
 */
@Import(ReceivableServiceImpl.class)
public class ReceivableServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ReceivableServiceImpl receivableService;

    @Resource
    private ReceivableMapper receivableMapper;

    @Test
    public void testCreateReceivable_success() {
        // 准备参数
        ReceivableCreateReqVO reqVO = randomPojo(ReceivableCreateReqVO.class);

        // 调用
        Long receivableId = receivableService.createReceivable(reqVO);
        // 断言
        assertNotNull(receivableId);
        // 校验记录的属性是否正确
        ReceivableDO receivable = receivableMapper.selectById(receivableId);
        assertPojoEquals(reqVO, receivable);
    }

    @Test
    public void testUpdateReceivable_success() {
        // mock 数据
        ReceivableDO dbReceivable = randomPojo(ReceivableDO.class);
        receivableMapper.insert(dbReceivable);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ReceivableUpdateReqVO reqVO = randomPojo(ReceivableUpdateReqVO.class, o -> {
            o.setId(dbReceivable.getId()); // 设置更新的 ID
        });

        // 调用
        receivableService.updateReceivable(reqVO);
        // 校验是否更新正确
        ReceivableDO receivable = receivableMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, receivable);
    }

    @Test
    public void testUpdateReceivable_notExists() {
        // 准备参数
        ReceivableUpdateReqVO reqVO = randomPojo(ReceivableUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> receivableService.updateReceivable(reqVO), RECEIVABLE_NOT_EXISTS);
    }

    @Test
    public void testDeleteReceivable_success() {
        // mock 数据
        ReceivableDO dbReceivable = randomPojo(ReceivableDO.class);
        receivableMapper.insert(dbReceivable);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbReceivable.getId();

        // 调用
        receivableService.deleteReceivable(id);
       // 校验数据不存在了
       assertNull(receivableMapper.selectById(id));
    }

    @Test
    public void testDeleteReceivable_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> receivableService.deleteReceivable(id), RECEIVABLE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetReceivablePage() {
       // mock 数据
       ReceivableDO dbReceivable = randomPojo(ReceivableDO.class, o -> { // 等会查询到
           o.setNo(null);
           o.setPlanId(null);
           o.setCustomerId(null);
           o.setContractId(null);
           o.setCheckStatus(null);
           o.setProcessInstanceId(null);
           o.setReturnTime(null);
           o.setReturnType(null);
           o.setPrice(null);
           o.setOwnerUserId(null);
           o.setBatchId(null);
           o.setSort(null);
           o.setDataScope(null);
           o.setDataScopeDeptIds(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       receivableMapper.insert(dbReceivable);
       // 测试 no 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setNo(null)));
       // 测试 planId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setPlanId(null)));
       // 测试 customerId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setCustomerId(null)));
       // 测试 contractId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setContractId(null)));
       // 测试 checkStatus 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setCheckStatus(null)));
       // 测试 processInstanceId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setProcessInstanceId(null)));
       // 测试 returnTime 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setReturnTime(null)));
       // 测试 returnType 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setReturnType(null)));
       // 测试 price 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setPrice(null)));
       // 测试 ownerUserId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setOwnerUserId(null)));
       // 测试 batchId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setBatchId(null)));
       // 测试 sort 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setSort(null)));
       // 测试 dataScope 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setDataScope(null)));
       // 测试 dataScopeDeptIds 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setDataScopeDeptIds(null)));
       // 测试 status 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setCreateTime(null)));
       // 准备参数
       ReceivablePageReqVO reqVO = new ReceivablePageReqVO();
       reqVO.setNo(null);
       reqVO.setPlanId(null);
       reqVO.setCustomerId(null);
       reqVO.setContractId(null);
       reqVO.setCheckStatus(null);
       reqVO.setProcessInstanceId(null);
       reqVO.setReturnTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setReturnType(null);
       reqVO.setPrice(null);
       reqVO.setOwnerUserId(null);
       reqVO.setBatchId(null);
       reqVO.setSort(null);
       reqVO.setDataScope(null);
       reqVO.setDataScopeDeptIds(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ReceivableDO> pageResult = receivableService.getReceivablePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbReceivable, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetReceivableList() {
       // mock 数据
       ReceivableDO dbReceivable = randomPojo(ReceivableDO.class, o -> { // 等会查询到
           o.setNo(null);
           o.setPlanId(null);
           o.setCustomerId(null);
           o.setContractId(null);
           o.setCheckStatus(null);
           o.setProcessInstanceId(null);
           o.setReturnTime(null);
           o.setReturnType(null);
           o.setPrice(null);
           o.setOwnerUserId(null);
           o.setBatchId(null);
           o.setSort(null);
           o.setDataScope(null);
           o.setDataScopeDeptIds(null);
           o.setStatus(null);
           o.setRemark(null);
           o.setCreateTime(null);
       });
       receivableMapper.insert(dbReceivable);
       // 测试 no 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setNo(null)));
       // 测试 planId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setPlanId(null)));
       // 测试 customerId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setCustomerId(null)));
       // 测试 contractId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setContractId(null)));
       // 测试 checkStatus 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setCheckStatus(null)));
       // 测试 processInstanceId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setProcessInstanceId(null)));
       // 测试 returnTime 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setReturnTime(null)));
       // 测试 returnType 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setReturnType(null)));
       // 测试 price 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setPrice(null)));
       // 测试 ownerUserId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setOwnerUserId(null)));
       // 测试 batchId 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setBatchId(null)));
       // 测试 sort 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setSort(null)));
       // 测试 dataScope 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setDataScope(null)));
       // 测试 dataScopeDeptIds 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setDataScopeDeptIds(null)));
       // 测试 status 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setStatus(null)));
       // 测试 remark 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setRemark(null)));
       // 测试 createTime 不匹配
       receivableMapper.insert(cloneIgnoreId(dbReceivable, o -> o.setCreateTime(null)));
       // 准备参数
       ReceivableExportReqVO reqVO = new ReceivableExportReqVO();
       reqVO.setNo(null);
       reqVO.setPlanId(null);
       reqVO.setCustomerId(null);
       reqVO.setContractId(null);
       reqVO.setCheckStatus(null);
       reqVO.setProcessInstanceId(null);
       reqVO.setReturnTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setReturnType(null);
       reqVO.setPrice(null);
       reqVO.setOwnerUserId(null);
       reqVO.setBatchId(null);
       reqVO.setSort(null);
       reqVO.setDataScope(null);
       reqVO.setDataScopeDeptIds(null);
       reqVO.setStatus(null);
       reqVO.setRemark(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<ReceivableDO> list = receivableService.getReceivableList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbReceivable, list.get(0));
    }

}
