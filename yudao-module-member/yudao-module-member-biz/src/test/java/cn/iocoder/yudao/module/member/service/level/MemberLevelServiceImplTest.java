package cn.iocoder.yudao.module.member.service.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.MemberLevelCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.MemberLevelPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.MemberLevelUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberLevelMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.LEVEL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MemberLevelServiceImpl} 的单元测试类
 *
 * @author owen
 */
@Import(MemberLevelServiceImpl.class)
public class MemberLevelServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MemberLevelServiceImpl levelService;

    @Resource
    private MemberLevelMapper levelMapper;

    @Test
    public void testCreateLevel_success() {
        // 准备参数
        MemberLevelCreateReqVO reqVO = randomPojo(MemberLevelCreateReqVO.class);

        // 调用
        Long levelId = levelService.createLevel(reqVO);
        // 断言
        assertNotNull(levelId);
        // 校验记录的属性是否正确
        MemberLevelDO level = levelMapper.selectById(levelId);
        assertPojoEquals(reqVO, level);
    }

    @Test
    public void testUpdateLevel_success() {
        // mock 数据
        MemberLevelDO dbLevel = randomPojo(MemberLevelDO.class);
        levelMapper.insert(dbLevel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MemberLevelUpdateReqVO reqVO = randomPojo(MemberLevelUpdateReqVO.class, o -> {
            o.setId(dbLevel.getId()); // 设置更新的 ID
        });

        // 调用
        levelService.updateLevel(reqVO);
        // 校验是否更新正确
        MemberLevelDO level = levelMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, level);
    }

    @Test
    public void testUpdateLevel_notExists() {
        // 准备参数
        MemberLevelUpdateReqVO reqVO = randomPojo(MemberLevelUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> levelService.updateLevel(reqVO), LEVEL_NOT_EXISTS);
    }

    @Test
    public void testDeleteLevel_success() {
        // mock 数据
        MemberLevelDO dbLevel = randomPojo(MemberLevelDO.class);
        levelMapper.insert(dbLevel);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbLevel.getId();

        // 调用
        levelService.deleteLevel(id);
        // 校验数据不存在了
        assertNull(levelMapper.selectById(id));
    }

    @Test
    public void testDeleteLevel_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> levelService.deleteLevel(id), LEVEL_NOT_EXISTS);
    }

    @Test
    public void testGetLevelPage() {
        // mock 数据
        MemberLevelDO dbLevel = randomPojo(MemberLevelDO.class, o -> { // 等会查询到
            o.setName("黄金会员");
            o.setStatus(1);
        });
        levelMapper.insert(dbLevel);
        // 测试 name 不匹配
        levelMapper.insert(cloneIgnoreId(dbLevel, o -> o.setName(null)));
        // 测试 status 不匹配
        levelMapper.insert(cloneIgnoreId(dbLevel, o -> o.setStatus(null)));
        // 准备参数
        MemberLevelPageReqVO reqVO = new MemberLevelPageReqVO();
        reqVO.setName("黄金会员");
        reqVO.setStatus(1);

        // 调用
        PageResult<MemberLevelDO> pageResult = levelService.getLevelPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbLevel, pageResult.getList().get(0));
    }

}
