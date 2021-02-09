package cn.iocoder.dashboard.modules.system.controller.logger;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.framework.logger.operatelog.core.annotations.OperateLog;
import cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum;
import cn.iocoder.dashboard.framework.logger.operatelog.core.util.OperateLogUtils;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogExcelVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.logger.vo.operatelog.SysOperateLogRespVO;
import cn.iocoder.dashboard.modules.system.convert.logger.SysOperateLogConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.logger.SysOperateLogDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.service.logger.SysOperateLogService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import cn.iocoder.dashboard.util.collection.MapUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "操作日志 API")
@RestController
@RequestMapping("/system/operate-log")
public class SysOperateLogController {

    @Resource
    private SysOperateLogService operateLogService;

    @Resource
    private SysUserService userService;

    @ApiOperation("示例")
    @OperateLog(type = OperateTypeEnum.OTHER)
    @GetMapping("/demo")
    public CommonResult<Boolean> demo() {
        // 这里可以调用业务逻辑

        // 补全操作日志的明细
        OperateLogUtils.setContent("将编号 1 的数据，xxx 字段修改成了 yyyy");
        OperateLogUtils.addExt("orderId", 1);

        // 响应
        return success(true);
    }

    @ApiOperation("查看操作日志分页列表")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:operate-log:query')")
    public CommonResult<PageResult<SysOperateLogRespVO>> pageOperateLog(@Validated SysOperateLogPageReqVO reqVO) {
        PageResult<SysOperateLogDO> pageResult = operateLogService.pageOperateLog(reqVO);

        // 获得拼接需要的数据
        Collection<Long> userIds = CollectionUtils.convertList(pageResult.getList(), SysOperateLogDO::getUserId);
        Map<Long, SysUserDO> userMap = userService.getUserMap(userIds);
        // 拼接数据
        List<SysOperateLogRespVO> list = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(operateLog -> {
            SysOperateLogRespVO respVO = SysOperateLogConvert.INSTANCE.convert(operateLog);
            list.add(respVO);
            // 拼接用户信息
            MapUtils.findAndThen(userMap, operateLog.getUserId(), user -> respVO.setUserNickname(user.getNickname()));
        });
        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @ApiOperation("导出操作日志")
    @GetMapping("/export")
    @OperateLog(type = EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:operate-log:export')")
    public void exportOperateLog(HttpServletResponse response, @Validated SysOperateLogExportReqVO reqVO)
            throws IOException {
        List<SysOperateLogDO> list = operateLogService.listOperateLogs(reqVO);

        // 获得拼接需要的数据
        Collection<Long> userIds = CollectionUtils.convertList(list, SysOperateLogDO::getUserId);
        Map<Long, SysUserDO> userMap = userService.getUserMap(userIds);
        // 拼接数据
        List<SysOperateLogExcelVO> excelDataList = SysOperateLogConvert.INSTANCE.convertList(list, userMap);
        // 输出
        ExcelUtils.write(response, "操作日志.xls", "数据列表",
                SysOperateLogExcelVO.class, excelDataList);
    }

}
