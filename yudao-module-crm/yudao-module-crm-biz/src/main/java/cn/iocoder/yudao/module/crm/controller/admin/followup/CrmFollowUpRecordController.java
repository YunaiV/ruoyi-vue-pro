package cn.iocoder.yudao.module.crm.controller.admin.followup;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.followup.CrmFollowUpRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;


@Tag(name = "管理后台 - 跟进记录")
@RestController
@RequestMapping("/crm/follow-up-record")
@Validated
public class CrmFollowUpRecordController {

    @Resource
    private CrmFollowUpRecordService crmFollowUpRecordService;
    @Resource
    private CrmContactService contactService;
    @Resource
    private CrmBusinessService businessService;

    @PostMapping("/create")
    @Operation(summary = "创建跟进记录")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:create')")
    public CommonResult<Long> createFollowUpRecord(@Valid @RequestBody CrmFollowUpRecordSaveReqVO createReqVO) {
        return success(crmFollowUpRecordService.createFollowUpRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新跟进记录")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:update')")
    public CommonResult<Boolean> updateFollowUpRecord(@Valid @RequestBody CrmFollowUpRecordSaveReqVO updateReqVO) {
        crmFollowUpRecordService.updateFollowUpRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除跟进记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:delete')")
    public CommonResult<Boolean> deleteFollowUpRecord(@RequestParam("id") Long id) {
        crmFollowUpRecordService.deleteFollowUpRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得跟进记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:query')")
    public CommonResult<CrmFollowUpRecordRespVO> getFollowUpRecord(@RequestParam("id") Long id) {
        CrmFollowUpRecordDO followUpRecord = crmFollowUpRecordService.getFollowUpRecord(id);
        return success(BeanUtils.toBean(followUpRecord, CrmFollowUpRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得跟进记录分页")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:query')")
    public CommonResult<PageResult<CrmFollowUpRecordRespVO>> getFollowUpRecordPage(@Valid CrmFollowUpRecordPageReqVO pageReqVO) {
        PageResult<CrmFollowUpRecordDO> pageResult = crmFollowUpRecordService.getFollowUpRecordPage(pageReqVO);
        Set<Long> contactIds = convertSetByFlatMap(pageResult.getList(), item -> item.getContactIds().stream());
        Set<Long> businessIds = convertSetByFlatMap(pageResult.getList(), item -> item.getBusinessIds().stream());
        Map<Long, CrmContactDO> contactMap = convertMap(contactService.getContactList(contactIds), CrmContactDO::getId);
        Map<Long, CrmBusinessDO> businessMap = convertMap(businessService.getBusinessList(businessIds), CrmBusinessDO::getId);
        PageResult<CrmFollowUpRecordRespVO> result = BeanUtils.toBean(pageResult, CrmFollowUpRecordRespVO.class);
        result.getList().forEach(item -> {
            setContactNames(item, contactMap);
            setBusinessNames(item, businessMap);
        });
        return success(result);
    }

    private static void setContactNames(CrmFollowUpRecordRespVO vo, Map<Long, CrmContactDO> contactMap) {
        List<String> names = new ArrayList<>();
        vo.getContactIds().forEach(id -> {
            MapUtils.findAndThen(contactMap, id, contactDO -> names.add(contactDO.getName()));
        });
        vo.setContactNames(names);
    }

    private static void setBusinessNames(CrmFollowUpRecordRespVO vo, Map<Long, CrmBusinessDO> businessMap) {
        List<String> names = new ArrayList<>();
        vo.getContactIds().forEach(id -> {
            MapUtils.findAndThen(businessMap, id, businessDO -> names.add(businessDO.getName()));
        });
        vo.setBusinessNames(names);
    }

}