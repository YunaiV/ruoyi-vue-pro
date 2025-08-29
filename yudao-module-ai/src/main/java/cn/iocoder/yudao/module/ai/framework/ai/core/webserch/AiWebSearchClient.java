package cn.iocoder.yudao.module.ai.framework.ai.core.webserch;

/**
 * 网络搜索客户端接口
 *
 * @author 芋道源码
 */
public interface AiWebSearchClient {

    /**
     * 网页搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    AiWebSearchResponse search(AiWebSearchRequest request);

}
