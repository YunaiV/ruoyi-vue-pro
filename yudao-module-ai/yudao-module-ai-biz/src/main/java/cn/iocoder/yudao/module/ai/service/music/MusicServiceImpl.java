package cn.iocoder.yudao.module.ai.service.music;

import cn.iocoder.yudao.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.SunoRespVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author xiaoxin
 * @Date 2024/5/29
 */
@Service
@RequiredArgsConstructor
public class MusicServiceImpl implements MusicService {

    private final SunoApi sunoApi;

    @Override
    public SunoRespVO musicGen(SunoReqVO sunoReqVO) {
        SunoApi.SunoResp sunoResp = sunoApi.musicGen(new SunoApi.SunoReq(
                sunoReqVO.getPrompt(),
                sunoReqVO.getLyric(),
                sunoReqVO.isCustom(),
                sunoReqVO.getTitle(),
                sunoReqVO.getStyle(),
                sunoReqVO.getCallbackUrl()
        ));
        return SunoRespVO.convertFrom(sunoResp);
    }
}
