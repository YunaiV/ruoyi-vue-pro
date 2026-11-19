
package cn.iocoder.yudao.module.ai.framework.ai.core.webserch;

/**
 * 网络搜索客户端接口：搜索、网页读取
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

    /**
     * 读取网页正文
     *
     * @param url 网页地址
     * @return 网页内容
     */
    AiWebFetchResponse fetch(String url);

}
