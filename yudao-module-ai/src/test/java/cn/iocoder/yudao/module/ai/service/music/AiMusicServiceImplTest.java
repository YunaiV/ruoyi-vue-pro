package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.MUSIC_NOT_EXISTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AiMusicServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiMusicServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;

    @InjectMocks
    private AiMusicServiceImpl musicService;

    @Mock
    private AiMusicMapper musicMapper;

    @Test
    public void testDeleteMusicMy_success() {
        // mock 数据
        Long id = randomLongId();
        when(musicMapper.selectById(id)).thenReturn(new AiMusicDO().setId(id).setUserId(USER_ID));

        // 调用
        musicService.deleteMusicMy(id, USER_ID);

        // 断言
        verify(musicMapper).deleteById(id);
    }

    @Test
    public void testDeleteMusicMy_notOwner() {
        // mock 数据
        Long id = randomLongId();
        when(musicMapper.selectById(id)).thenReturn(new AiMusicDO().setId(id).setUserId(USER_ID));

        // 调用，并断言异常
        assertServiceException(() -> musicService.deleteMusicMy(id, 2L), MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testDeleteMusicMy_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> musicService.deleteMusicMy(id, USER_ID), MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).deleteById(any(Long.class));
    }

}
