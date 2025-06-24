package cn.iocoder.yudao.module.huawei.smarthome.controller.app.room;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomListBySpaceReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomListRespDTO;
import cn.iocoder.yudao.module.huawei.smarthome.dto.room.RoomUpdateReqDTO;
import cn.iocoder.yudao.module.huawei.smarthome.service.room.HuaweiRoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户APP - 华为智能家居 - 子空间/区域管理")
@RestController
@RequestMapping("/huawei/smarthome/space/{spaceId}/room") // 基础路径包含spaceId
@Validated
public class AppHuaweiRoomController {

    @Resource
    private HuaweiRoomService huaweiRoomService;

    @GetMapping("/list")
    @ApiOperation("查询空间内子空间信息")
    public CommonResult<RoomListRespDTO> listRoomsBySpace(
            @ApiParam(value = "空间 ID", required = true, example = "877422008017620608")
            @PathVariable("spaceId") String spaceId) {
        try {
            RoomListBySpaceReqDTO reqDTO = new RoomListBySpaceReqDTO(spaceId);
            RoomListRespDTO respDTO = huaweiRoomService.listRoomsBySpaceId(reqDTO);
            return success(respDTO);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API查询子空间列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/update-name") // 使用POST模拟PUT
    @ApiOperation("修改子空间名称")
    public CommonResult<?> updateRoomName(
            @ApiParam(value = "空间 ID", required = true, example = "877422003647155840")
            @PathVariable("spaceId") String spaceId,
            @Valid @RequestBody RoomUpdateReqDTO updateReqDTO) {
        // 确保路径中的spaceId与请求体中的spaceId（如果设计为包含）一致，或以此为准
        updateReqDTO.setSpaceId(spaceId); // 以路径参数为准
        try {
            huaweiRoomService.updateRoomName(updateReqDTO);
            return success(null);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API修改子空间名称失败: " + e.getMessage());
        }
    }
}
