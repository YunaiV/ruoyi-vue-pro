package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// TODO @haohao：有部分非实体的部分，是不是可以搞到 iot 的 framework 包下，搞个 tdengine 包，作为框架级的封装，放在 dataobject，感觉不是很合理哈。【可以微信讨论下】
/**
 * TdRestApi 类用于处理 TDengine 的 REST API 请求
 */
@Slf4j
@Service
@Deprecated // TODO 芋艿：貌似没用到
public class TdRestApi {

    @Value("${spring.datasource.dynamic.datasource.tdengine.url}")
    private String url;

    @Value("${spring.datasource.dynamic.datasource.tdengine.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.tdengine.password}")
    private String password;

    /**
     * 获取 REST API URL
     */
    private String getRestApiUrl() {
        String restUrl = url.replace("jdbc:TAOS-RS://", "")
                .replaceAll("\\?.*", "");
        int idx = restUrl.lastIndexOf("/");
        return String.format("%s/rest/sql/%s", restUrl.substring(0, idx), restUrl.substring(idx + 1));
    }


    /**
     * 新建td api请求对象
     */
    public HttpRequest newApiRequest(String sql) {
        return HttpRequest
                .post(getRestApiUrl())
                .body(sql)
                .basicAuth(username, password);
    }

    /**
     * 执行sql
     */
    public TdResponse execSql(String sql) {
        log.info("exec td sql:{}", sql);
        HttpRequest request = newApiRequest(sql);
        HttpResponse response = request.execute();
        return JSONUtil.toBean(response.body(), TdResponse.class);
    }

}