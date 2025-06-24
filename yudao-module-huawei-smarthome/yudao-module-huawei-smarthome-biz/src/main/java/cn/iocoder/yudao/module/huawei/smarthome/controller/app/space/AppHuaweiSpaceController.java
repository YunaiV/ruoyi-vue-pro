package cn.iocoder.yudao.module.huawei.smarthome.controller.app.space;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.huawei.smarthome.dto.space.*;
import cn.iocoder.yudao.module.huawei.smarthome.service.space.HuaweiSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户APP - 华为智能家居 - 空间管理")
@RestController
@RequestMapping("/huawei/smarthome/space")
@Validated
public class AppHuaweiSpaceController {

    @Resource
    private HuaweiSpaceService huaweiSpaceService;

    @PostMapping("/create")
    @ApiOperation("创建空间")
    public CommonResult<SpaceCreateRespDTO> createSpace(@Valid @RequestBody SpaceCreateReqDTO createReqDTO) {
        try {
            SpaceCreateRespDTO respDTO = huaweiSpaceService.createSpace(createReqDTO);
            return success(respDTO);
        } catch (IOException e) {
            // TODO: 转换为更友好的错误提示或特定的业务异常
            // 例如，可以根据 e.getMessage() 中的特定错误码返回不同的错误信息
            // 此处暂时简单处理
            return CommonResult.error(500, "调用华为API创建空间失败: " + e.getMessage());
        }
    }

    @PostMapping("/update") // 使用POST模拟PUT，或直接使用PutMapping
    @ApiOperation("修改空间")
    public CommonResult<?> updateSpace(@Valid @RequestBody SpaceUpdateReqDTO updateReqDTO) {
        try {
            huaweiSpaceService.updateSpace(updateReqDTO);
            return success(null); // 修改成功通常不返回数据
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API修改空间失败: " + e.getMessage());
        }
    }

    @PostMapping("/delete") // 使用POST模拟DELETE，或直接使用DeleteMapping
    @ApiOperation("删除空间")
    public CommonResult<?> deleteSpace(@Valid @RequestBody SpaceDeleteReqDTO deleteReqDTO) {
        try {
            huaweiSpaceService.deleteSpace(deleteReqDTO);
            return success(null); // 删除成功通常不返回数据
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API删除空间失败: " + e.getMessage());
        }
    }

    @GetMapping("/list-all")
    @ApiOperation("查询项目下所有空间信息")
    public CommonResult<SpaceListRespDTO> listAllSpaces() {
        try {
            SpaceListRespDTO respDTO = huaweiSpaceService.listAllSpaces();
            return success(respDTO);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API查询所有空间失败: " + e.getMessage());
        }
    }

    @GetMapping("/list-by-ids")
    @ApiOperation("查询指定的多个空间信息")
    // 前端通常通过逗号分隔的字符串传递列表，或者重复参数名
    // 这里使用 @RequestParam List<String> spaceIds 来接收多个同名参数
    public CommonResult<SpaceListRespDTO> listSpacesByIds(@RequestParam("spaceId") List<String> spaceIds) {
        try {
            SpaceListRespDTO respDTO = huaweiSpaceService.listSpacesByIds(spaceIds);
            return success(respDTO);
        } catch (IOException e) {
            return CommonResult.error(500, "调用华为API查询指定空间失败: " + e.getMessage());
        }
    }
}
