package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AiMusicServiceImpl#deleteMusicMy(Long, Long)} 的单元测试类
 *
 * 删除别人的曲子时，抛出的错误身份必须是「音乐不存在」而不是「图片不存在」：
 * 同文件其它分支用的都是 MUSIC_NOT_EXISTS，错误的族会让调用方按错误码排查时被指到图片模块。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AiMusicDeleteMyGuardTest {

    @Mock
    private AiMusicMapper musicMapper;

    @InjectMocks
    private AiMusicServiceImpl musicService;

    private AiMusicDO music(Long userId) {
        AiMusicDO music = new AiMusicDO();
        music.setId(11L);
        music.setUserId(userId);
        return music;
    }

    @Test
    public void testDeleteMusicMy_otherUser() {
        when(musicMapper.selectById(11L)).thenReturn(music(1L));

        // 断言错误身份是音乐族，且没有落删除
        assertServiceException(() -> musicService.deleteMusicMy(11L, 2L),
                ErrorCodeConstants.MUSIC_NOT_EXISTS);
        verify(musicMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testDeleteMusicMy_own() {
        when(musicMapper.selectById(11L)).thenReturn(music(1L));

        musicService.deleteMusicMy(11L, 1L);

        verify(musicMapper).deleteById(11L);
    }

}
