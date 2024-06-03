package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

import cn.iocoder.yudao.framework.ai.core.model.suno.api.AceDataSunoApi;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * API 响应的数据
 */
@Data
public class SunoRespVO {
    /**
     * 表示请求是否成功
     */
    private boolean success;

    /**
     * 任务 ID
     */
    @JsonProperty("task_id")
    private String taskId;

    /**
     * 音乐数据列表
     */
    private List<MusicDataVO> data;


    //把 SunoResp转为本vo类
    public static SunoRespVO convertFrom(AceDataSunoApi.SunoResp sunoResp) {
        SunoRespVO sunoRespVO = new SunoRespVO();
        sunoRespVO.setSuccess(sunoResp.success());
        sunoRespVO.setTaskId(sunoResp.taskId());
        sunoRespVO.setData(MusicDataVO.convertFrom(sunoResp.data()));
        return sunoRespVO;
    }

}