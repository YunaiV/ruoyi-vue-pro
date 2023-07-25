package cn.iocoder.yudao.module.system.controller.admin.ip;

import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.framework.ip.core.utils.IPUtils;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.ip.vo.AreaNodeSimpleRespVO;
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

    @GetMapping("/get-children")
    @Operation(summary = "获得地区的下级区域")
    @Parameter(name = "id", description = "区域编号", required = true, example = "150000")
    public CommonResult<List<AreaNodeSimpleRespVO>> getChildren(@RequestParam("id") Integer id) {
        Area area = AreaUtils.getArea(id);
        Assert.notNull(area, String.format("获取不到 id : %d 的区域", id));
        return success(AreaConvert.INSTANCE.convertList2(area.getChildren()));
    }

    // 4)方法改成 getAreaChildrenList 获得子节点们；5）url 可以已改成 children-list
    //@芋艿 是不是叫 getAreaListByIds 更合适。 因为不一定是子节点。 用于前端树选择获取缓存数据。 见 <el-tree-select :cache-data="areaCache">
    @GetMapping("/get-by-ids")
    @Operation(summary = "通过区域 ids 获得地区列表")
    @Parameter(name = "ids", description = "区域编号 ids", required = true, example = "1,150000")
    public CommonResult<List<AreaNodeSimpleRespVO>> getAreaListByIds(@RequestParam("ids") Set<Integer> ids) {
        List<Area> areaList = new ArrayList<>(ids.size());
        for (Integer areaId : ids) {
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
