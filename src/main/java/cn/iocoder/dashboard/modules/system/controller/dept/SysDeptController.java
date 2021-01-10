package cn.iocoder.dashboard.modules.system.controller.dept;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptListReqVO;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptRespVO;
import cn.iocoder.dashboard.modules.system.controller.dept.vo.dept.SysDeptSimpleRespVO;
import cn.iocoder.dashboard.modules.system.convert.dept.SysDeptConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "部门 API")
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    @Resource
    private SysDeptService deptService;

    @ApiOperation("获取部门列表")
//    @PreAuthorize("@ss.hasPermi('system:dept:list')")
    @GetMapping("/list")
    public CommonResult<List<SysDeptRespVO>> listDepts(SysDeptListReqVO reqVO) {
        List<SysDeptDO> list = deptService.listDepts(reqVO);
        list.sort(Comparator.comparing(SysDeptDO::getSort));
        return success(SysDeptConvert.INSTANCE.convertList(list));
    }

    @ApiOperation(value = "获取部门精简信息列表", notes = "只包含被开启的部门，主要用于前端的下拉选项")
    @GetMapping("/list-all-simple")
    public CommonResult<List<SysDeptSimpleRespVO>> listSimpleDepts() {
        // 获得部门列表，只要开启状态的
        SysDeptListReqVO reqVO = new SysDeptListReqVO();
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<SysDeptDO> list = deptService.listDepts(reqVO);
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SysDeptDO::getSort));
        return success(SysDeptConvert.INSTANCE.convertList02(list));
    }

}
