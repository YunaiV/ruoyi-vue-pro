package cn.iocoder.yudao.module.crm.controller.admin.followup;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.followup.CrmFollowUpRecordService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
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

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建跟进记录")
    public CommonResult<Long> createFollowUpRecord(@Valid @RequestBody CrmFollowUpRecordSaveReqVO createReqVO) {
        return success(followUpRecordService.createFollowUpRecord(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除跟进记录")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteFollowUpRecord(@RequestParam("id") Long id) {
        followUpRecordService.deleteFollowUpRecord(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得跟进记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<CrmFollowUpRecordRespVO> getFollowUpRecord(@RequestParam("id") Long id) {
        CrmFollowUpRecordDO followUpRecord = followUpRecordService.getFollowUpRecord(id);
        return success(BeanUtils.toBean(followUpRecord, CrmFollowUpRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得跟进记录分页")
    public CommonResult<PageResult<CrmFollowUpRecordRespVO>> getFollowUpRecordPage(@Valid CrmFollowUpRecordPageReqVO pageReqVO) {
        PageResult<CrmFollowUpRecordDO> pageResult = followUpRecordService.getFollowUpRecordPage(pageReqVO);
        // 1.1 查询联系人和商机
        Map<Long, CrmContactDO> contactMap = contactService.getContactMap(
                convertSetByFlatMap(pageResult.getList(), item -> item.getContactIds().stream()));
        Map<Long, CrmBusinessDO> businessMap = businessService.getBusinessMap(
                convertSetByFlatMap(pageResult.getList(), item -> item.getBusinessIds().stream()));
        // 1.2 查询用户
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(pageResult.getList(), item -> Long.valueOf(item.getCreator())));
        // 2. 拼接数据
        PageResult<CrmFollowUpRecordRespVO> voPageResult = BeanUtils.toBean(pageResult, CrmFollowUpRecordRespVO.class, record -> {
            // 2.1 设置联系人和商机信息
            record.setBusinesses(new ArrayList<>()).setContacts(new ArrayList<>());
            record.getContactIds().forEach(id -> MapUtils.findAndThen(contactMap, id, contact ->
                    record.getContacts().add(new CrmBusinessRespVO().setId(contact.getId()).setName(contact.getName()))));
            record.getBusinessIds().forEach(id -> MapUtils.findAndThen(businessMap, id, business ->
                    record.getBusinesses().add(new CrmBusinessRespVO().setId(business.getId()).setName(business.getName()))));
            // 2.2 设置用户信息
            MapUtils.findAndThen(userMap, Long.valueOf(record.getCreator()), user -> record.setCreatorName(user.getNickname()));
        });
        return success(voPageResult);
    }

}
