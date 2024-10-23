package cn.iocoder.yudao.module.iot.dal.dataobject.tdengine;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * TdRestApi 类用于处理 TDengine 的 REST API 请求
 */
@Slf4j
@Service
public class TdRestApi {

    @Value("${spring.datasource.dynamic.datasource.master.url}")
    private String url;

    @Value("${spring.datasource.dynamic.datasource.master.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.master.password}")
    private String password;

    private String getRestApiUrl() {
        //jdbc:TAOS-RS://127.0.0.1:6041/iotkit?xxxx
        String restUrl = url.replace("jdbc:TAOS-RS://" , "")
                .replaceAll("\\?.*" , "");
        // /rest/sql/iotkit
        int idx = restUrl.lastIndexOf("/");
        //127.0.0.1:6041/rest/sql/iotkit
        return String.format("%s/rest/sql/%s" , restUrl.substring(0, idx), restUrl.substring(idx + 1));
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
        log.info("exec td sql:{}" , sql);
        HttpRequest request = newApiRequest(sql);
        HttpResponse response = request.execute();
        return JSONUtil.toBean(response.body(), TdResponse.class);
    }


}
