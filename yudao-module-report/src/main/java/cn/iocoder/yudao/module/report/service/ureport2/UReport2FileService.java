package cn.iocoder.yudao.module.report.service.ureport2;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FilePageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport2.UReport2FileDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * UReport2 报表模板 Service 接口
 *
 * 模板按当前租户隔离，租户由数据库多租户插件自动处理
 *
 * @author 芋道源码
 */
public interface UReport2FileService {

    /**
     * 保存模板：不存在时创建，存在时更新内容；保存后，发布模板的新版本号
     *
     * @param saveReqVO 保存信息
     * @return 本次保存发布的版本号
     */
    String saveFile(@Valid UReport2FileSaveReqVO saveReqVO);

    /**
     * 删除模板；删除后，发布模板的新版本号
     *
     * @param name 模板名称，不包含存储前缀
     */
    void deleteFileByName(String name);

    /**
     * 删除模板；删除后，发布模板的新版本号
     *
     * @param id 编号
     */
    void deleteFile(Long id);

    /**
     * 获得模板
     *
     * @param name 模板名称，不包含存储前缀
     * @return 模板
     */
    UReport2FileDO getFileByName(String name);

    /**
     * 获得模板列表，不包含模板内容
     *
     * @return 模板列表
     */
    List<UReport2FileDO> getFileList();

    /**
     * 获得模板分页，不包含模板内容
     *
     * @param pageReqVO 分页查询
     * @return 模板分页
     */
    PageResult<UReport2FileDO> getFilePage(UReport2FilePageReqVO pageReqVO);

}
