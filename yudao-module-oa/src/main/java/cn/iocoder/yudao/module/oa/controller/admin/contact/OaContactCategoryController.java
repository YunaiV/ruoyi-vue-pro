package cn.iocoder.yudao.module.oa.controller.admin.contact;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category.OaContactCategoryRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.contact.vo.category.OaContactCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.contact.OaContactCategoryDO;
import cn.iocoder.yudao.module.oa.service.contact.OaContactCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 联系人分类")
@RestController
@RequestMapping("/oa/contact-category")
@Validated
public class OaContactCategoryController {

    @Resource
    private OaContactCategoryService contactCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建联系人分类")
    public CommonResult<Long> createContactCategory(
            @Valid @RequestBody OaContactCategorySaveReqVO createReqVO) {
        return success(contactCategoryService.createContactCategory(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新联系人分类")
    public CommonResult<Boolean> updateContactCategory(
            @Valid @RequestBody OaContactCategorySaveReqVO updateReqVO) {
        contactCategoryService.updateContactCategory(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除联系人分类")
    @Parameter(name = "id", description = "分类编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteContactCategory(@RequestParam("id") Long id) {
        contactCategoryService.deleteContactCategory(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得联系人分类")
    @Parameter(name = "id", description = "分类编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:contact:query')")
    public CommonResult<OaContactCategoryRespVO> getContactCategory(@RequestParam("id") Long id) {
        OaContactCategoryDO category = contactCategoryService.getContactCategory(id, getLoginUserId());
        return success(BeanUtils.toBean(category, OaContactCategoryRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得分类精简列表")
    public CommonResult<List<OaContactCategoryRespVO>> getSimpleContactCategoryList() {
        List<OaContactCategoryDO> categories = contactCategoryService.getContactCategoryList(getLoginUserId());
        return success(convertList(categories, category -> new OaContactCategoryRespVO()
                .setId(category.getId()).setName(category.getName())));
    }

    @GetMapping("/list")
    @Operation(summary = "获得联系人分类列表")
    @PreAuthorize("@ss.hasPermission('oa:contact:query')")
    public CommonResult<List<OaContactCategoryRespVO>> getContactCategoryList() {
        List<OaContactCategoryDO> categories = contactCategoryService.getContactCategoryList(getLoginUserId());
        return success(BeanUtils.toBean(categories, OaContactCategoryRespVO.class));
    }

}
