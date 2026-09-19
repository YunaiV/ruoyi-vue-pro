package cn.iocoder.yudao.module.oa.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileFavoriteDO;
import cn.iocoder.yudao.module.oa.dal.mysql.file.OaFileFavoriteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaFileFavoriteServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaFileFavoriteServiceImpl.class)
public class OaFileFavoriteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaFileFavoriteService fileFavoriteService;

    @Resource
    private OaFileFavoriteMapper fileFavoriteMapper;

    @MockBean
    private OaFileNodeService fileNodeService;

    @Test
    public void testGetFileFavoriteListByUserIdAndNodeIds() {
        // mock 数据：仅返回本人、当前页、未逻辑删除的收藏
        OaFileFavoriteDO matched = randomFileFavoriteDO(10L, 1L);
        fileFavoriteMapper.insert(matched);
        fileFavoriteMapper.insert(randomFileFavoriteDO(10L, 2L));
        fileFavoriteMapper.insert(randomFileFavoriteDO(11L, 1L));
        OaFileFavoriteDO deleted = randomFileFavoriteDO(12L, 1L);
        fileFavoriteMapper.insert(deleted);
        fileFavoriteMapper.deleteById(deleted.getId());

        // 调用
        List<OaFileFavoriteDO> result = fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(1L, Arrays.asList(10L, 12L));
        // 断言
        assertEquals(1, result.size());
        assertEquals(matched.getId(), CollUtil.getFirst(result).getId());
    }

    @Test
    public void testGetFileFavoriteListByUserIdAndNodeIds_empty() {
        // mock 数据
        fileFavoriteMapper.insert(randomFileFavoriteDO(10L, 1L));

        // 调用
        List<OaFileFavoriteDO> result = fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(1L, Collections.emptyList());
        // 断言：空页不能退化成查询全部收藏
        assertTrue(CollUtil.isEmpty(result));
    }

    @Test
    public void testCreateFileFavorite_repeatAndUserIsolation() {
        // mock 数据
        OaFileFavoriteDO other = randomFileFavoriteDO(10L, 2L);
        fileFavoriteMapper.insert(other);

        // 调用
        fileFavoriteService.createFileFavorite(10L, 1L);
        fileFavoriteService.createFileFavorite(10L, 1L);
        // 断言
        assertEquals(2, fileFavoriteMapper.selectList().size());
        assertNotNull(fileFavoriteMapper.selectByUserIdAndNodeId(1L, 10L));
        assertNotNull(fileFavoriteMapper.selectById(other.getId()));
        verify(fileNodeService, times(2)).validateFileNodePermission(10L, 1L, 1);
    }

    @Test
    public void testDeleteFileFavorite_onlyCurrentUser() {
        // mock 数据
        OaFileFavoriteDO own = randomFileFavoriteDO(10L, 1L);
        OaFileFavoriteDO other = randomFileFavoriteDO(10L, 2L);
        fileFavoriteMapper.insert(own);
        fileFavoriteMapper.insert(other);

        // 调用
        fileFavoriteService.deleteFileFavorite(10L, 1L);
        // 断言
        assertNull(fileFavoriteMapper.selectById(own.getId()));
        assertNotNull(fileFavoriteMapper.selectById(other.getId()));
    }

    @Test
    public void testDeleteFileFavoritesByNodeIds() {
        // mock 数据
        OaFileFavoriteDO first = randomFileFavoriteDO(10L, 1L);
        OaFileFavoriteDO second = randomFileFavoriteDO(11L, 2L);
        OaFileFavoriteDO retained = randomFileFavoriteDO(12L, 1L);
        fileFavoriteMapper.insert(first);
        fileFavoriteMapper.insert(second);
        fileFavoriteMapper.insert(retained);

        // 调用
        fileFavoriteService.deleteFileFavoritesByNodeIds(Collections.emptyList());
        fileFavoriteService.deleteFileFavoritesByNodeIds(Arrays.asList(10L, 11L));
        // 断言
        assertNull(fileFavoriteMapper.selectById(first.getId()));
        assertNull(fileFavoriteMapper.selectById(second.getId()));
        assertNotNull(fileFavoriteMapper.selectById(retained.getId()));
    }

    // ========== 随机对象 ==========

    /**
     * 构造文件收藏关系。
     *
     * @param nodeId 文件节点编号
     * @param userId 用户编号
     * @return 未入库的测试对象
     */
    private static OaFileFavoriteDO randomFileFavoriteDO(Long nodeId, Long userId) {
        return randomPojo(OaFileFavoriteDO.class, favorite -> {
            favorite.setNodeId(nodeId);
            favorite.setUserId(userId);
        });
    }

}
