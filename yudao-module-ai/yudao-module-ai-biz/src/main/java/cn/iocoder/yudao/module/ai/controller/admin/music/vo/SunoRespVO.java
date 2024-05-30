package cn.iocoder.yudao.module.ai.controller.admin.music.vo;

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

}