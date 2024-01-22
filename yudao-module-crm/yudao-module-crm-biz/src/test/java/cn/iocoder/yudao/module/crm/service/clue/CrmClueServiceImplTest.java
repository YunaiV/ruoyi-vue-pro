package cn.iocoder.yudao.module.crm.service.clue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmCluePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CLUE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

// TODO 芋艿：单测后续补；

/**
 * {@link CrmClueServiceImpl} 的单元测试类
 *
 * @author Wanwan
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(CrmClueServiceImpl.class)
public class CrmClueServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CrmClueServiceImpl clueService;

    @Resource
    private CrmClueMapper clueMapper;

    @Test
    public void testCreateClue_success() {
        // 准备参数
        CrmClueSaveReqVO reqVO = randomPojo(CrmClueSaveReqVO.class);

        // 调用
        Long clueId = clueService.createClue(reqVO, getLoginUserId());
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
        CrmClueSaveReqVO reqVO = randomPojo(CrmClueSaveReqVO.class, o -> {
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
        CrmClueSaveReqVO reqVO = randomPojo(CrmClueSaveReqVO.class);

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
        // 测试 contactLastTime 不匹配
        clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setContactLastTime(null)));
        // 测试 createTime 不匹配
        clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setCreateTime(null)));
        // 准备参数
        CrmCluePageReqVO reqVO = new CrmCluePageReqVO();
        reqVO.setName(null);
        reqVO.setTelephone(null);
        reqVO.setMobile(null);

        // 调用
        PageResult<CrmClueDO> pageResult = clueService.getCluePage(reqVO, 1L);
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
        // 测试 contactLastTime 不匹配
        clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setContactLastTime(null)));
        // 测试 createTime 不匹配
        clueMapper.insert(cloneIgnoreId(dbClue, o -> o.setCreateTime(null)));
        // 准备参数
        CrmCluePageReqVO reqVO = new CrmCluePageReqVO();
        reqVO.setName(null);
        reqVO.setTelephone(null);
        reqVO.setMobile(null);
        reqVO.setPageSize(PAGE_SIZE_NONE);
        // 调用
        List<CrmClueDO> list = clueService.getCluePage(reqVO, 1L).getList();
        // 断言
        assertEquals(1, list.size());
        assertPojoEquals(dbClue, list.get(0));
    }

}
