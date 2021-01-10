package cn.iocoder.dashboard.modules.system.controller.dept;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.post.SysPostSimpleRespVO;
import cn.iocoder.dashboard.modules.system.convert.dept.SysPostConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysPostDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysPostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "岗位 API")
@RestController
@RequestMapping("/system/post")
public class SysPostController {

    @Resource
    private SysPostService postService;

    @ApiOperation(value = "获取岗位精简信息列表", notes = "只包含被开启的岗位，主要用于前端的下拉选项")
    @GetMapping("/list-all-simple")
    public CommonResult<List<SysPostSimpleRespVO>> listSimplePosts() {
        // 获得岗位列表，只要开启状态的
        List<SysPostDO> list = postService.listPosts(null, Collections.singleton(CommonStatusEnum.ENABLE.getStatus()));
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SysPostDO::getSort));
        return success(SysPostConvert.INSTANCE.convertList02(list));
    }

}
