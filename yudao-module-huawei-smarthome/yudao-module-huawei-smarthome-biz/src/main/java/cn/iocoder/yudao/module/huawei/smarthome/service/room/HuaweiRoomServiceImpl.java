package cn.iocoder.yudao.module.huawei.smarthome.service.room;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomListBySpaceReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomListRespDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomUpdateReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.framework.core.HuaweiSmartHomeAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HuaweiRoomServiceImpl implements HuaweiRoomService {

    @Resource
    private HuaweiSmartHomeAuthClient huaweiSmartHomeAuthClient;

    private static final String ROOM_BASE_PATH_FORMAT = "/openapi/asset/v1/space/%s/rooms"; // 查询
    private static final String ROOM_UPDATE_PATH_FORMAT = "/openapi/asset/v1/space/%s/room"; // 修改

    @Override
    public RoomListRespDTO listRoomsBySpaceId(RoomListBySpaceReqDTO listReqDTO) throws IOException {
        String path = String.format(ROOM_BASE_PATH_FORMAT, listReqDTO.getSpaceId());
        String responseBody = huaweiSmartHomeAuthClient.get(path);
        return JsonUtils.parseObject(responseBody, RoomListRespDTO.class);
    }

    @Override
    public void updateRoomName(RoomUpdateReqDTO updateReqDTO) throws IOException {
        String path = String.format(ROOM_UPDATE_PATH_FORMAT, updateReqDTO.getSpaceId());
        // 根据华为文档，PUT /openapi/asset/v1/space/{spaceId}/room 的请求体只需要 roomName 和 desc
        // roomId 是用来标识要修改哪个房间，但它并不在请求体中，而是隐含在业务逻辑中或通过其他方式（如果API支持）
        // 华为文档的示例请求体是：{ "roomName": "阳台", "desc": "" }
        // 它没有包含 roomId。这意味着 spaceId + roomName (旧) 或者 spaceId + roomId (如果API内部通过其他参数知道roomId) 来定位。
        // 查阅文档，请求参数中有 roomId，但请求示例的 body 中没有 roomId。
        // PUT /openapi/asset/v1/space/{spaceId}/room
        // 请求参数： spaceId, roomId, roomName, desc
        // 这里的 roomId 应该是指要修改的房间的ID，roomName 是新的名称。
        // 华为的示例请求是 PUT https://{Endpoint}/openapi/asset/v1/space/877422003647155840/room
        // { "roomName": "阳台", "desc": "" }
        // 这意味着 roomId 必须通过其他方式传递或服务器能从 spaceId 和其他上下文推断。
        // 通常，如果API是 /space/{spaceId}/room/{roomId} 这样的，就很清晰。
        // 但它是 /space/{spaceId}/room，请求体里有 roomName, desc。
        // 仔细看文档 "2.2.2 修改子空间名称", 其请求参数表格列出了 spaceId, roomId, roomName, desc。
        // 这通常意味着这些参数都应该在请求的某个部分。如果不在URL路径中，那么就在请求体中或作为查询参数。
        // 鉴于其是PUT请求，并且示例请求体中只有roomName和desc，这很可能是文档的一个不一致之处，或者roomId是作为查询参数？
        // 但RESTful PUT通常将所有要修改的资源标识和属性放在URL或请求体。
        // 假设文档的请求参数列表是准确的，并且它们都应在请求体中（除了URL中的spaceId）。
        // 更新：再次确认华为文档，修改子空间名称的 PUT 请求，其 Request Body 的示例是：
        // { "roomName": "阳台", "desc": "" }
        // 而在请求参数表格中，列出了 spaceId, roomId, roomName, desc.
        // 这表明 spaceId 是 path variable. roomId, roomName, desc 应该是 body parameters.
        // 但是示例的 body 只有 roomName, desc.
        // 这非常令人困惑。一种可能是 roomId 不是必需的，如果 roomName 在 spaceId 下是唯一的。
        // 另一种可能是，虽然文档列出了 roomId，但实际API实现中它可能通过其他方式处理，或者示例不完整。
        // 按照最安全的假设，如果参数在"请求参数"列表中，它就应该被发送。
        // 如果示例请求体是对的，那么只需要发送 roomName 和 desc。
        // 我们将遵循示例请求体，因为它更具体地展示了API的实际用法。
        // 如果需要roomId，华为API应该从路径或特定头部获取，或者它是一个隐含的标识。
        // 为了安全，我们创建一个只包含文档示例中字段的Map作为请求体。
        Map<String, String> requestBodyMap = new HashMap<>();
        requestBodyMap.put("roomName", updateReqDTO.getRoomName());
        if (updateReqDTO.getDesc() != null) { // desc 是可选的
            requestBodyMap.put("desc", updateReqDTO.getDesc());
        }
        // 如果华为API确实需要roomId在body中，而文档示例省略了，那么需要在此处添加：
        // requestBodyMap.put("roomId", updateReqDTO.getRoomId());

        String requestBody = JsonUtils.toJsonString(requestBodyMap);
        huaweiSmartHomeAuthClient.put(path, requestBody);
        // PUT请求成功通常返回200 OK，没有响应体或响应体为空
    }
}
