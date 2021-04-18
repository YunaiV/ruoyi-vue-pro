package cn.iocoder.dashboard.modules.system.controller.errorcode;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodeCreateDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodePageDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodeUpdateDTO;
import cn.iocoder.dashboard.modules.system.controller.errorcode.vo.ErrorCodeVO;
import cn.iocoder.dashboard.modules.system.service.errorcode.impl.ErrorCodeServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "错误码")
@RestController
@RequestMapping("/system/error-code")
public class ErrorCodeController {

    @Resource
    private ErrorCodeServiceImpl errorCodeService;

    /**
     * 创建错误码
     *
     * @param createDTO 创建错误码 DTO
     * @return 错误码编号
     */
    @ApiOperation("创建错误码")
    @PostMapping("/create")
    public CommonResult<Integer> createErrorCode(@RequestBody ErrorCodeCreateDTO createDTO) {
        return success(errorCodeService.createErrorCode(createDTO).getId());
    }

    /**
     * 更新错误码
     *
     * @param updateDTO 更新错误码 DTO
     */
    @ApiOperation("更新错误码")
    @PatchMapping("/update")
    public CommonResult<Boolean> updateErrorCode(@RequestBody ErrorCodeUpdateDTO updateDTO) {
        errorCodeService.updateErrorCode(updateDTO);
        return success(Boolean.TRUE);
    }

    /**
     * 删除错误码
     *
     * @param errorCodeId 错误码编号
     */
    @ApiOperation("删除错误码")
    @DeleteMapping("delete")
    public CommonResult<Boolean> deleteErrorCode(Integer errorCodeId) {
        errorCodeService.deleteErrorCode(errorCodeId);
        return success(Boolean.TRUE);
    }

    /**
     * 获得错误码
     *
     * @param errorCodeId 错误码编号
     * @return 错误码
     */
    @ApiOperation("获取错误码")
    @GetMapping("/query")
    public CommonResult<ErrorCodeVO> getErrorCode(Integer errorCodeId) {
        return success(errorCodeService.getErrorCode(errorCodeId));
    }

    /**
     * 获得错误码列表
     *
     * @param errorCodeIds 错误码编号列表
     * @return 错误码列表
     */
    @ApiOperation("获取错误码列表")
    @GetMapping("/query-ids")
    public CommonResult<List<ErrorCodeVO>> listErrorCodes(@RequestBody List<Integer> errorCodeIds) {
        return success(errorCodeService.listErrorCodes(errorCodeIds));
    }

    /**
     * 获得错误码分页
     *
     * @param pageDTO 错误码分页查询
     * @return 错误码分页结果
     */
    @ApiOperation("获取错误码分页列表")
    @GetMapping("/page")
    public CommonResult<PageResult<ErrorCodeVO>> pageErrorCode(ErrorCodePageDTO pageDTO) {
        return success(errorCodeService.pageErrorCode(pageDTO));
    }

}
