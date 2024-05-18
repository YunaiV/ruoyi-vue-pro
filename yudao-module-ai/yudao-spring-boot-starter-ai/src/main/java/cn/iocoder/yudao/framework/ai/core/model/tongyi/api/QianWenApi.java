package cn.iocoder.yudao.framework.ai.core.model.tongyi.api;

import cn.iocoder.yudao.framework.ai.core.model.tongyi.QianWenChatModal;
import cn.iocoder.yudao.framework.ai.core.exception.AiException;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

// TODO done @fansili：是不是挪到 api 包里？按照 spring ai 的结构；根目录只放 client 和 options

/**
 * 阿里 通义千问
 * <p>
 * author: fansili
 * time: 2024/3/13 21:09
 */
@Getter
public class QianWenApi {

    // api key 获取地址：https://bailian.console.aliyun.com/?spm=5176.28197581.0.0.38db29a4G3GcVb&apiKey=1#/api-key
    private String apiKey = "sk-Zsd81gZYg7";
    private Generation gen = new Generation();
    private QianWenChatModal qianWenChatModal;

    public QianWenApi(String apiKey, QianWenChatModal qianWenChatModal) {
        this.apiKey = apiKey;
        this.qianWenChatModal = qianWenChatModal;
    }

    public ResponseEntity<GenerationResult> chatCompletionEntity(QwenParam request) {
        GenerationResult call;
        try {
            call = gen.call(request);
        } catch (NoApiKeyException e) {
            throw new AiException("没有找到apiKey！" + e.getMessage());
        } catch (InputRequiredException e) {
            throw new AiException("chat缺少必填字段！" + e.getMessage());
        }
        // 阿里云的这个 http code 随便设置，外面判断是否成功用的 CompletionsResponse.isSuccess
        return new ResponseEntity<>(call, HttpStatusCode.valueOf(200));
    }

    public Flowable<GenerationResult> chatCompletionStream(QwenParam request) {
        Flowable<GenerationResult> resultFlowable;
        try {
            resultFlowable = gen.streamCall(request);
        } catch (NoApiKeyException e) {
            throw new AiException("没有找到apiKey！" + e.getMessage());
        } catch (InputRequiredException e) {
            throw new AiException("chat缺少必填字段！" + e.getMessage());
        }
        return resultFlowable;
    }
}
