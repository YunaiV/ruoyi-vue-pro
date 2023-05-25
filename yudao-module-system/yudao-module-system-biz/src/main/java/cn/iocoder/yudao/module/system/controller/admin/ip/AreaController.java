package cn.iocoder.yudao.module.system.controller.admin.ip;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.ip.core.utils.IPUtils;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.LazyAreaNodeRespVO;
import cn.iocoder.yudao.module.system.convert.ip.AreaConvert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 地区")
@RestController
@RequestMapping("/system/area")
@Validated
public class AreaController {

    @GetMapping("/tree")
    @Operation(summary = "获得地区树")
    public CommonResult<List<AreaNodeRespVO>> getAreaTree() {
        Area area = AreaUtils.getArea(Area.ID_CHINA);
        Assert.notNull(area, "获取不到中国");
        return success(AreaConvert.INSTANCE.convertList(area.getChildren()));
    }

    // TODO @jason：1）url 使用中划线分隔哈，然后可以改成 children；2）id 需要添加 @RequestParam，因为可能会混淆编译；
    // 3) swagger 注解要写下哈；
    @GetMapping("/getChildrenArea")
    @Operation(summary = "获得地区的下级区域")
    public CommonResult<List<LazyAreaNodeRespVO>> getChildrenArea(Integer id) {
        Area area = AreaUtils.getArea(id);
        Assert.notNull(area, String.format("获取不到 id : %d的区域", id));
        return success(AreaConvert.INSTANCE.convertList2(area.getChildren()));
    }

    // TODO @jason：1）读请求，使用 get 哈。2）然后参数不应该使用 @RequestBody；3）areaIds 改成 ids 更合适；
    // 4)方法改成 getAreaChildrenList 获得子节点们；5）url 可以已改成 children-list
    @PostMapping("/list")
    @Operation(summary = "通过区域 ids 获得地区列表")
    public CommonResult<List<LazyAreaNodeRespVO>> list(@RequestBody Set<Integer> areaIds) {
        List<Area> areaList = new ArrayList<>(areaIds.size());
        for (Integer areaId : areaIds) {
            areaList.add(AreaUtils.getArea(areaId));
        }
        return success(AreaConvert.INSTANCE.convertList2(areaList));
    }

    @GetMapping("/get-by-ip")
    @Operation(summary = "获得 IP 对应的地区名")
    @Parameter(name = "ip", description = "IP", required = true)
    public CommonResult<String> getAreaByIp(@RequestParam("ip") String ip) {
        // 获得城市
        Area area = IPUtils.getArea(ip);
        if (area == null) {
            return success("未知");
        }
        // 格式化返回
        return success(AreaUtils.format(area.getId()));
    }

}
