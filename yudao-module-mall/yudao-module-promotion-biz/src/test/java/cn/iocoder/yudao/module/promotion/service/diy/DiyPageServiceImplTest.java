package cn.iocoder.yudao.module.promotion.service.diy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPageCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPageUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyPageMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_PAGE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DiyPageServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Disabled // TODO 芋艿：后续 fix 补充的单测
@Import(DiyPageServiceImpl.class)
public class DiyPageServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DiyPageServiceImpl diyPageService;

    @Resource
    private DiyPageMapper diyPageMapper;

    @Test
    public void testCreateDiyPage_success() {
        // 准备参数
        DiyPageCreateReqVO reqVO = randomPojo(DiyPageCreateReqVO.class);

        // 调用
        Long diyPageId = diyPageService.createDiyPage(reqVO);
        // 断言
        assertNotNull(diyPageId);
        // 校验记录的属性是否正确
        DiyPageDO diyPage = diyPageMapper.selectById(diyPageId);
        assertPojoEquals(reqVO, diyPage);
    }

    @Test
    public void testUpdateDiyPage_success() {
        // mock 数据
        DiyPageDO dbDiyPage = randomPojo(DiyPageDO.class);
        diyPageMapper.insert(dbDiyPage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DiyPageUpdateReqVO reqVO = randomPojo(DiyPageUpdateReqVO.class, o -> {
            o.setId(dbDiyPage.getId()); // 设置更新的 ID
        });

        // 调用
        diyPageService.updateDiyPage(reqVO);
        // 校验是否更新正确
        DiyPageDO diyPage = diyPageMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, diyPage);
    }

    @Test
    public void testUpdateDiyPage_notExists() {
        // 准备参数
        DiyPageUpdateReqVO reqVO = randomPojo(DiyPageUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> diyPageService.updateDiyPage(reqVO), DIY_PAGE_NOT_EXISTS);
    }

    @Test
    public void testDeleteDiyPage_success() {
        // mock 数据
        DiyPageDO dbDiyPage = randomPojo(DiyPageDO.class);
        diyPageMapper.insert(dbDiyPage);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDiyPage.getId();

        // 调用
        diyPageService.deleteDiyPage(id);
        // 校验数据不存在了
        assertNull(diyPageMapper.selectById(id));
    }

    @Test
    public void testDeleteDiyPage_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> diyPageService.deleteDiyPage(id), DIY_PAGE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDiyPagePage() {
        // mock 数据
        DiyPageDO dbDiyPage = randomPojo(DiyPageDO.class, o -> { // 等会查询到
            o.setName(null);
            o.setCreateTime(null);
        });
        diyPageMapper.insert(dbDiyPage);
        // 测试 name 不匹配
        diyPageMapper.insert(cloneIgnoreId(dbDiyPage, o -> o.setName(null)));
        // 测试 createTime 不匹配
        diyPageMapper.insert(cloneIgnoreId(dbDiyPage, o -> o.setCreateTime(null)));
        // 准备参数
        DiyPagePageReqVO reqVO = new DiyPagePageReqVO();
        reqVO.setName(null);
        reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

        // 调用
        PageResult<DiyPageDO> pageResult = diyPageService.getDiyPagePage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDiyPage, pageResult.getList().get(0));
    }

}
