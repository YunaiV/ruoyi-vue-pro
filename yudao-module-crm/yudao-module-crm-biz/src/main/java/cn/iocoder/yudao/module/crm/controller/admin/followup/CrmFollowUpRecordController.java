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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;


@Tag(name = "管理后台 - 跟进记录")
@RestController
@RequestMapping("/crm/follow-up-record")
@Validated
public class CrmFollowUpRecordController {

    @Resource
    private CrmFollowUpRecordService followUpRecordService;
    @Resource
    private CrmContactService contactService;
    @Resource
    private CrmBusinessService businessService;

    @PostMapping("/create")
    @Operation(summary = "创建跟进记录")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:create')")
    public CommonResult<Long> createFollowUpRecord(@Valid @RequestBody CrmFollowUpRecordSaveReqVO createReqVO) {
        return success(followUpRecordService.createFollowUpRecord(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除跟进记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:delete')")
    public CommonResult<Boolean> deleteFollowUpRecord(@RequestParam("id") Long id) {
        followUpRecordService.deleteFollowUpRecord(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得跟进记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:query')")
    public CommonResult<CrmFollowUpRecordRespVO> getFollowUpRecord(@RequestParam("id") Long id) {
        CrmFollowUpRecordDO followUpRecord = followUpRecordService.getFollowUpRecord(id);
        return success(BeanUtils.toBean(followUpRecord, CrmFollowUpRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得跟进记录分页")
    @PreAuthorize("@ss.hasPermission('crm:follow-up-record:query')")
    public CommonResult<PageResult<CrmFollowUpRecordRespVO>> getFollowUpRecordPage(@Valid CrmFollowUpRecordPageReqVO pageReqVO) {
        PageResult<CrmFollowUpRecordDO> pageResult = followUpRecordService.getFollowUpRecordPage(pageReqVO);
        /// 拼接数据
        Map<Long, CrmContactDO> contactMap = convertMap(contactService.getContactListByIds(
                convertSetByFlatMap(pageResult.getList(), item -> item.getContactIds().stream())), CrmContactDO::getId);
        Map<Long, CrmBusinessDO> businessMap = convertMap(businessService.getBusinessList(
                convertSetByFlatMap(pageResult.getList(), item -> item.getBusinessIds().stream())), CrmBusinessDO::getId);
        PageResult<CrmFollowUpRecordRespVO> voPageResult = BeanUtils.toBean(pageResult, CrmFollowUpRecordRespVO.class, record -> {
            record.setContactNames(new ArrayList<>()).setBusinessNames(new ArrayList<>());
            record.getContactIds().forEach(id -> MapUtils.findAndThen(contactMap, id,
                    contact -> record.getContactNames().add(contact.getName())));
            record.getContactIds().forEach(id -> MapUtils.findAndThen(businessMap, id,
                    business -> record.getBusinessNames().add(business.getName())));
        });
        return success(voPageResult);
    }

}