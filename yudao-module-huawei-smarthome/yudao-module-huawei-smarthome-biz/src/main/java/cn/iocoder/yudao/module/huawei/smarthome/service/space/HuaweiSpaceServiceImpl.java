package cn.iocoder.yudao.module.huawei.smarthome.service.space;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.huawei.smarthome.dto.space.SpaceCreateReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.space.SpaceCreateRespDTO;
import cn.iocoder.yudao.module.huawei.smarthome.framework.core.HuaweiSmartHomeAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;

@Service
@Slf4j
public class HuaweiSpaceServiceImpl implements HuaweiSpaceService {

    @Resource
    private HuaweiSmartHomeAuthClient huaweiSmartHomeAuthClient;

    private static final String SPACE_BASE_PATH = "/openapi/asset/v1/space";

    @Override
    public SpaceCreateRespDTO createSpace(SpaceCreateReqDTO createReqDTO) throws IOException {
        String requestBody = JsonUtils.toJsonString(createReqDTO);
        String responseBody = huaweiSmartHomeAuthClient.post(SPACE_BASE_PATH, requestBody);
        return JsonUtils.parseObject(responseBody, SpaceCreateRespDTO.class);
    }

    @Override
    public void updateSpace(SpaceUpdateReqDTO updateReqDTO) throws IOException {
        String requestBody = JsonUtils.toJsonString(updateReqDTO);
        // 修改空间API的路径与创建空间相同，但使用PUT方法
        huaweiSmartHomeAuthClient.put(SPACE_BASE_PATH, requestBody);
        // PUT请求成功通常返回200 OK，没有响应体或响应体为空，这里不解析响应体
    }

    @Override
    public void deleteSpace(SpaceDeleteReqDTO deleteReqDTO) throws IOException {
        // 删除空间的API路径为 /openapi/asset/v1/space/{spaceId}
        String deletePath = SPACE_BASE_PATH + "/" + deleteReqDTO.getSpaceId();
        huaweiSmartHomeAuthClient.delete(deletePath);
        // DELETE请求成功通常返回204 No Content，没有响应体
    }

    @Override
    public SpaceListRespDTO listAllSpaces() throws IOException {
        String responseBody = huaweiSmartHomeAuthClient.get(SPACE_BASE_PATH + "s"); // URI: /openapi/asset/v1/spaces
        return JsonUtils.parseObject(responseBody, SpaceListRespDTO.class);
    }

    @Override
    public SpaceListRespDTO listSpacesByIds(List<String> spaceIds) throws IOException {
        if (spaceIds == null || spaceIds.isEmpty()) {
            // 或者根据业务需求返回空列表或抛出异常
            return new SpaceListRespDTO();
        }
        // 构建查询参数: ?spaceId=x1&spaceId=x2
        StringBuilder queryParams = new StringBuilder("?");
        for (int i = 0; i < spaceIds.size(); i++) {
            queryParams.append("spaceId=").append(spaceIds.get(i));
            if (i < spaceIds.size() - 1) {
                queryParams.append("&");
            }
        }
        String pathWithParams = SPACE_BASE_PATH + "s" + queryParams.toString(); // URI: /openapi/asset/v1/spaces?spaceId=...
        String responseBody = huaweiSmartHomeAuthClient.get(pathWithParams);
        return JsonUtils.parseObject(responseBody, SpaceListRespDTO.class);
    }
}
