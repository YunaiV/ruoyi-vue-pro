package cn.iocoder.yudao.module.oa.service.seal;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealDO;
import cn.iocoder.yudao.module.oa.dal.mysql.seal.OaSealMapper;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaSealServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaSealServiceImpl.class)
public class OaSealServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaSealService sealService;

    @Resource
    private OaSealMapper sealMapper;

    @MockBean
    private OaNoRedisDAO noRedisDAO;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private DeptApi deptApi;
    @MockBean
    private OaSealApplyService sealApplyService;

    @Test
    public void testDeleteSeal_inUse() {
        // mock 数据
        OaSealDO record = randomSealDO();
        sealMapper.insert(record);
        // mock 方法
        when(sealApplyService.getSealApplyCountBySealId(record.getId())).thenReturn(1L);

        // 调用，并断言异常
        assertServiceException(() -> sealService.deleteSeal(record.getId()), SEAL_IN_USE);
        assertNotNull(sealMapper.selectById(record.getId()));
    }

    @Test
    public void testCreateSeal() {
        // mock 单号生成
        when(noRedisDAO.generate(OaNoRedisDAO.SEAL_NO_PREFIX)).thenReturn("SEAL20260912000001");
        // 准备参数
        OaSealSaveReqVO reqVO = toBean(randomSealDO(), OaSealSaveReqVO.class).setStatus(1);

        // 调用
        Long id = sealService.createSeal(reqVO);
        // 断言
        OaSealDO seal = sealMapper.selectById(id);
        assertEquals("SEAL20260912000001", seal.getNo());
        assertEquals(1, seal.getStatus());
        assertEquals(reqVO.getDeptId(), seal.getDeptId());
        verify(deptApi).validateDeptList(Collections.singleton(30L));
        verify(adminUserApi).validateUser(10L);
    }

    @Test
    public void testUpdateSeal_keeperChanged() {
        // mock 数据
        OaSealDO seal = randomSealDO();
        sealMapper.insert(seal);
        // 准备参数
        OaSealSaveReqVO reqVO = toBean(seal, OaSealSaveReqVO.class).setKeeperUserId(11L);

        // 调用
        sealService.updateSeal(reqVO);
        // 断言
        assertEquals(11L, sealMapper.selectById(seal.getId()).getKeeperUserId());
        assertEquals(seal.getNo(), sealMapper.selectById(seal.getId()).getNo());
        verifyNoInteractions(noRedisDAO);
        verify(adminUserApi).validateUser(11L);
    }

    @Test
    public void testDeleteSeal() {
        // mock 数据
        OaSealDO seal = randomSealDO();
        sealMapper.insert(seal);

        // 调用
        sealService.deleteSeal(seal.getId());
        // 断言
        assertNull(sealMapper.selectById(seal.getId()));
        assertServiceException(() -> sealService.deleteSeal(seal.getId()), SEAL_NOT_EXISTS);
    }

    @Test
    public void testUpdateSeal_status() {
        // mock 数据
        OaSealDO seal = randomSealDO();
        sealMapper.insert(seal);

        // 调用
        sealService.updateSeal(toBean(seal, OaSealSaveReqVO.class).setStatus(1));
        // 断言
        assertEquals(1, sealMapper.selectById(seal.getId()).getStatus());
        assertEquals(seal.getDisableTime(), sealMapper.selectById(seal.getId()).getDisableTime());
    }

    @Test
    public void testUpdateSeal_status_inUseToAvailable() {
        // mock 数据
        OaSealDO seal = randomSealDO().setStatus(2);
        sealMapper.insert(seal);

        // 调用
        sealService.updateSeal(toBean(seal, OaSealSaveReqVO.class).setStatus(0));
        // 断言
        assertEquals(0, sealMapper.selectById(seal.getId()).getStatus());
    }

    @Test
    public void testGetSealPage_deptId() {
        // mock 数据
        OaSealDO seal = randomSealDO().setDeptId(30L).setKeeperDeptId(20L);
        sealMapper.insert(seal);
        sealMapper.insert(randomSealDO().setDeptId(31L).setKeeperDeptId(30L));
        // 准备参数
        OaSealPageReqVO reqVO = new OaSealPageReqVO().setDeptId(30L);

        // 调用
        PageResult<OaSealDO> pageResult = sealService.getSealPage(reqVO);
        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(seal.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testGetSealPage_deptIdEmpty() {
        // mock 数据
        sealMapper.insert(randomSealDO().setDeptId(30L));
        sealMapper.insert(randomSealDO().setDeptId(31L));
        // 准备参数
        OaSealPageReqVO reqVO = new OaSealPageReqVO();

        // 调用
        PageResult<OaSealDO> pageResult = sealService.getSealPage(reqVO);
        // 断言
        assertEquals(2L, pageResult.getTotal());
    }

    @Test
    public void testGetSealPage_sort() {
        // mock 数据：编号和插入顺序均不能替代显示顺序，相同顺序按编号倒序
        sealMapper.insert(randomSealDO().setId(10L).setSort(20));
        sealMapper.insert(randomSealDO().setId(20L).setSort(10));
        sealMapper.insert(randomSealDO().setId(30L).setSort(10));
        // 准备参数
        OaSealPageReqVO reqVO = new OaSealPageReqVO();

        // 调用
        PageResult<OaSealDO> pageResult = sealService.getSealPage(reqVO);
        // 断言
        assertEquals(Arrays.asList(30L, 20L, 10L),
                CollectionUtils.convertList(
                        pageResult.getList(), OaSealDO::getId));
    }

    @Test
    public void testGetSealPage_filters() {
        // mock 数据：分别排除类型、保管人和三个时间范围不匹配的印章
        LocalDateTime beginTime = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 30, 23, 59, 59);
        OaSealDO seal = randomSealDO().setType(1).setKeeperUserId(10L)
                .setPurchaseTime(beginTime).setEnableTime(endTime).setDisableTime(endTime);
        sealMapper.insert(seal);
        sealMapper.insert(toBean(seal, OaSealDO.class).setId(null).setNo("FILTER-TYPE").setType(2));
        sealMapper.insert(toBean(seal, OaSealDO.class).setId(null).setNo("FILTER-KEEPER").setKeeperUserId(11L));
        sealMapper.insert(toBean(seal, OaSealDO.class).setId(null).setNo("FILTER-PURCHASE")
                .setPurchaseTime(beginTime.minusSeconds(1)));
        sealMapper.insert(toBean(seal, OaSealDO.class).setId(null).setNo("FILTER-ENABLE")
                .setEnableTime(endTime.plusSeconds(1)));
        sealMapper.insert(toBean(seal, OaSealDO.class).setId(null).setNo("FILTER-DISABLE")
                .setDisableTime(endTime.plusSeconds(1)));
        // 准备参数
        OaSealPageReqVO reqVO = new OaSealPageReqVO().setType(1).setKeeperUserId(10L)
                .setPurchaseTime(new LocalDateTime[]{beginTime, endTime})
                .setEnableTime(new LocalDateTime[]{beginTime, endTime})
                .setDisableTime(new LocalDateTime[]{beginTime, endTime});

        // 调用
        PageResult<OaSealDO> pageResult = sealService.getSealPage(reqVO);
        // 断言：时间范围包含首尾边界
        assertEquals(1L, pageResult.getTotal());
        assertEquals(seal.getId(), CollUtil.getFirst(pageResult.getList()).getId());
    }

    @Test
    public void testUpdateSeal_deptId() {
        // mock 数据
        OaSealDO seal = randomSealDO();
        sealMapper.insert(seal);
        // 准备参数
        OaSealSaveReqVO reqVO = toBean(seal, OaSealSaveReqVO.class).setDeptId(31L);

        // 调用
        sealService.updateSeal(reqVO);
        // 断言：变更所属部门不修改保管部门
        OaSealDO updated = sealMapper.selectById(seal.getId());
        assertEquals(31L, updated.getDeptId());
        assertEquals(seal.getKeeperDeptId(), updated.getKeeperDeptId());
        verify(deptApi).validateDeptList(Collections.singleton(31L));
    }

    // ========== 随机对象 ==========

    /**
     * 构造具有指定保管人的可用印章
     *
     * @return 未入库的测试对象
     */
    private static OaSealDO randomSealDO() {
        return randomPojo(OaSealDO.class, seal -> seal.setId(null).setType(1).setCategory(1)
                .setDeptId(30L).setKeeperUserId(10L).setKeeperDeptId(20L).setStatus(0).setSort(0));
    }
}
