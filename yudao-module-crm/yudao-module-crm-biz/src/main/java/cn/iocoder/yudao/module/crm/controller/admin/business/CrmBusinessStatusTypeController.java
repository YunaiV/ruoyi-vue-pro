package cn.iocoder.yudao.module.crm.controller.admin.business;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusQueryVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeQueryVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeSaveReqVO;
import cn.iocoder.yudao.module.crm.convert.businessstatus.CrmBusinessStatusConvert;
import cn.iocoder.yudao.module.crm.convert.businessstatustype.CrmBusinessStatusTypeConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusService;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusTypeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - CRM 商机状态类型")
@RestController
@RequestMapping("/crm/business-status-type")
@Validated
public class CrmBusinessStatusTypeController {

    @Resource
    private CrmBusinessStatusTypeService businessStatusTypeService;

    @Resource
    private CrmBusinessStatusService businessStatusService;

    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建商机状态类型")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:create')")
    public CommonResult<Long> createBusinessStatusType(@Valid @RequestBody CrmBusinessStatusTypeSaveReqVO createReqVO) {
        return success(businessStatusTypeService.createBusinessStatusType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商机状态类型")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:update')")
    public CommonResult<Boolean> updateBusinessStatusType(@Valid @RequestBody CrmBusinessStatusTypeSaveReqVO updateReqVO) {
        businessStatusTypeService.updateBusinessStatusType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商机状态类型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:delete')")
    public CommonResult<Boolean> deleteBusinessStatusType(@RequestParam("id") Long id) {
        businessStatusTypeService.deleteBusinessStatusType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得商机状态类型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<CrmBusinessStatusTypeRespVO> getBusinessStatusType(@RequestParam("id") Long id) {
        CrmBusinessStatusTypeDO statusType = businessStatusTypeService.getBusinessStatusType(id);
        // 处理状态回显
        // TODO @lzxhqs：可以在 businessStatusService 加个 getBusinessStatusListByTypeId 方法，直接返回 List<CrmBusinessStatusDO> 哈，常用的，尽量封装个简单易懂的方法，不用追求绝对通用哈；
        CrmBusinessStatusQueryVO queryVO = new CrmBusinessStatusQueryVO();
        queryVO.setTypeId(id);
        List<CrmBusinessStatusDO> statusList = businessStatusService.selectList(queryVO);
        return success(CrmBusinessStatusTypeConvert.INSTANCE.convert(statusType, statusList));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商机状态类型分页")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<PageResult<CrmBusinessStatusTypeRespVO>> getBusinessStatusTypePage(@Valid CrmBusinessStatusTypePageReqVO pageReqVO) {
        PageResult<CrmBusinessStatusTypeDO> pageResult = businessStatusTypeService.getBusinessStatusTypePage(pageReqVO);
        // 处理部门回显
        Set<Long> deptIds = CollectionUtils.convertSetByFlatMap(pageResult.getList(), CrmBusinessStatusTypeDO::getDeptIds,Collection::stream);
        List<DeptRespDTO> deptList = deptApi.getDeptList(deptIds);
        return success(CrmBusinessStatusTypeConvert.INSTANCE.convertPage(pageResult, deptList));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商机状态类型 Excel")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:export')")
    @OperateLog(type = EXPORT)
    public void exportBusinessStatusTypeExcel(@Valid CrmBusinessStatusTypePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.getBusinessStatusTypePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "商机状态类型.xls", "数据", CrmBusinessStatusTypeRespVO.class,
                        BeanUtils.toBean(list, CrmBusinessStatusTypeRespVO.class));
    }

    @GetMapping("/get-simple-list")
    @Operation(summary = "获得商机状态类型列表")
    @PreAuthorize("@ss.hasPermission('crm:business-status-type:query')")
    public CommonResult<List<CrmBusinessStatusTypeRespVO>> getBusinessStatusTypeList() {
        CrmBusinessStatusTypeQueryVO queryVO = new CrmBusinessStatusTypeQueryVO();
        queryVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        List<CrmBusinessStatusTypeDO> list = businessStatusTypeService.selectList(queryVO);
        return success(BeanUtils.toBean(list, CrmBusinessStatusTypeRespVO.class));
    }

    // TODO @ljlleo 这个接口，是不是可以和 getBusinessStatusTypeList 合并成一个？
    @GetMapping("/get-status-list")
    @Operation(summary = "获得商机状态列表")
    @PreAuthorize("@ss.hasPermission('crm:business-status:query')")
    public CommonResult<List<CrmBusinessStatusRespVO>> getBusinessStatusListByTypeId(@RequestParam("typeId") Long typeId) {
        CrmBusinessStatusQueryVO queryVO = new CrmBusinessStatusQueryVO();
        queryVO.setTypeId(typeId);
        List<CrmBusinessStatusDO> list = businessStatusService.selectList(queryVO);
        return success(CrmBusinessStatusConvert.INSTANCE.convertList(list));
    }

}
