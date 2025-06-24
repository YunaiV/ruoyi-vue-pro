package cn.iocoder.yudao.module.huawei.smarthome.service.room;

import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomListBySpaceReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomListRespDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomUpdateReqDTO;

import javax.validation.Valid;
import java.io.IOException;

/**
 * 华为智能家居 - 子空间/区域管理 Service 接口
 *
 * @author Jules
 */
public interface HuaweiRoomService {

    /**
     * 查询空间内子空间信息
     *
     * @param listReqDTO 包含 spaceId 的请求 DTO
     * @return 子空间信息列表
     * @throws IOException 当 API 调用失败时抛出
     */
    RoomListRespDTO listRoomsBySpaceId(@Valid RoomListBySpaceReqDTO listReqDTO) throws IOException;

    /**
     * 修改子空间名称
     *
     * @param updateReqDTO 修改子空间名称请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void updateRoomName(@Valid RoomUpdateReqDTO updateReqDTO) throws IOException;

}
