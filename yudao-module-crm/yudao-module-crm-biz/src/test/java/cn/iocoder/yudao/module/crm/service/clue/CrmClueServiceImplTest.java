package cn.iocoder.yudao.module.crm.service.clue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;
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
 * {@link CrmClueServiceImpl} 的单元测试类
 *
 * @author Wanwan
 */
@Import(CrmClueServiceImpl.class)
public class CrmClueServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmClueServiceImpl clueService;

    @Resource
    private CrmClueMapper clueMapper;

    @Test
    public void testCreateClue_success() {
        // 准备参数
        CrmClueCreateReqVO reqVO = randomPojo(CrmClueCreateReqVO.class);

        // 调用
        Long clueId = clueService.createClue(reqVO);
        // 断言
        assertNotNull(clueId);
        // 校验记录的属性是否正确
        CrmClueDO clue = clueMapper.selectById(clueId);
        assertPojoEquals(reqVO, clue);
    }

    @Test
    public void testUpdateClue_success() {
        // mock 数据
        CrmClueDO dbClue = randomPojo(CrmClueDO.class);
        clueMapper.insert(dbClue);// @Sql: 先插入出一条存在的数据
        // 准备参数
        CrmClueUpdateReqVO reqVO = randomPojo(CrmClueUpdateReqVO.class, o -> {
            o.setId(dbClue.getId()); // 设置更新的 ID
        });

        // 调用
        clueService.updateClue(reqVO);
        // 校验是否更新正确
        CrmClueDO clue = clueMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, clue);
    }

    @Test
    public void testUpdateClue_notExists() {
        // 准备参数
        CrmClueUpdateReqVO reqVO = randomPojo(CrmClueUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> clueService.updateClue(reqVO), CLUE_NOT_EXISTS);
    }

    @Test
    public void testDeleteClue_success() {
        // mock 数据
        CrmClueDO dbClue = randomPojo(CrmClueDO.class);
        clueMapper.insert(dbClue);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbClue.getId();

        // 调用
        clueService.deleteClue(id);
       // 校验数据不存在了
       assertNull(clueMapper.selectById(id));
    }

    @Test
    public void testDeleteClue_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> clueService.deleteClue(id), CLUE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetCluePage() {
       // mock 数据
       CrmClueDO dbClue = randomPojo(CrmClueDO.class, o -> { // 等会查询到
           o.setTransformStatus(null);
           o.setFollowUpStatus(null);
           o.setName(null);
           o.setCustomerId(null);
           o.setContactNextTime(null);
           o.setTelephone(null);
           o.setMobile(null);
           o.setAddress(null);
           o.setOwnerUserId(null);
           o.setContactLastTime(null);
           o.setCreateTime(null);
       });
       clueMapper.insert(dbClue);
       // 测试 transformStatus 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setTransformStatus(null)));
       // 测试 followUpStatus 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setFollowUpStatus(null)));
       // 测试 name 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setName(null)));
       // 测试 customerId 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setCustomerId(null)));
       // 测试 contactNextTime 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setContactNextTime(null)));
       // 测试 telephone 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setTelephone(null)));
       // 测试 mobile 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setMobile(null)));
       // 测试 address 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setAddress(null)));
       // 测试 ownerUserId 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setOwnerUserId(null)));
       // 测试 contactLastTime 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setContactLastTime(null)));
       // 测试 createTime 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setCreateTime(null)));
       // 准备参数
       CrmCluePageReqVO reqVO = new CrmCluePageReqVO();
       reqVO.setTransformStatus(null);
       reqVO.setFollowUpStatus(null);
       reqVO.setName(null);
       reqVO.setCustomerId(null);
       reqVO.setContactNextTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setTelephone(null);
       reqVO.setMobile(null);
       reqVO.setAddress(null);
       reqVO.setOwnerUserId(null);
       reqVO.setContactLastTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<CrmClueDO> pageResult = clueService.getCluePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbClue, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetClueList() {
       // mock 数据
       CrmClueDO dbClue = randomPojo(CrmClueDO.class, o -> { // 等会查询到
           o.setTransformStatus(null);
           o.setFollowUpStatus(null);
           o.setName(null);
           o.setCustomerId(null);
           o.setContactNextTime(null);
           o.setTelephone(null);
           o.setMobile(null);
           o.setAddress(null);
           o.setOwnerUserId(null);
           o.setContactLastTime(null);
           o.setCreateTime(null);
       });
       clueMapper.insert(dbClue);
       // 测试 transformStatus 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setTransformStatus(null)));
       // 测试 followUpStatus 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setFollowUpStatus(null)));
       // 测试 name 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setName(null)));
       // 测试 customerId 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setCustomerId(null)));
       // 测试 contactNextTime 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setContactNextTime(null)));
       // 测试 telephone 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setTelephone(null)));
       // 测试 mobile 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setMobile(null)));
       // 测试 address 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setAddress(null)));
       // 测试 ownerUserId 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setOwnerUserId(null)));
       // 测试 contactLastTime 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setContactLastTime(null)));
       // 测试 createTime 不匹配
       clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setCreateTime(null)));
       // 准备参数
       CrmClueExportReqVO reqVO = new CrmClueExportReqVO();
       reqVO.setTransformStatus(null);
       reqVO.setFollowUpStatus(null);
       reqVO.setName(null);
       reqVO.setCustomerId(null);
       reqVO.setContactNextTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setTelephone(null);
       reqVO.setMobile(null);
       reqVO.setAddress(null);
       reqVO.setOwnerUserId(null);
       reqVO.setContactLastTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<CrmClueDO> list = clueService.getClueList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbClue, list.get(0));
    }

}
