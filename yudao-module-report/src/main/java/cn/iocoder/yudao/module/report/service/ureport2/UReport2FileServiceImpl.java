package cn.iocoder.yudao.module.report.service.ureport2;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FilePageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FileSaveReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport2.UReport2FileDO;
import cn.iocoder.yudao.module.report.dal.mysql.ureport2.UReport2FileMapper;
import cn.iocoder.yudao.module.report.dal.redis.ureport2.UReport2FileVersionRedisDAO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.cache.UReport2ReportDefinitionCache;
import cn.iocoder.yudao.module.report.framework.ureport2.core.provider.UReport2DatabaseReportProvider;
import cn.iocoder.yudao.module.report.framework.ureport2.core.util.UReport2FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.UREPORT2_FILE_NOT_EXISTS;

/**
 * UReport2 报表模板 Service 实现类
 *
 * 模板名称都先通过 {@link UReport2FileNameUtils#normalize(String)} 规范化，保证与数据库的比较规则一致
 *
 * 保存、删除时，先写入数据库（没有外层事务，自动提交），再通过 {@link UReport2FileVersionRedisDAO} 发布新的版本号，各实例的模板缓存据此失效。
 * 删除时，还会移除本实例的模板缓存：设计器、管理后台的删除，都经过 {@link #deleteFile(UReport2FileDO)}
 * 同名模板的并发保存、删除不做额外处理。
 * 注意：数据库与 Redis 不是同一个事务。如果数据库已提交、发布版本号失败（例如说 Redis 此时不可用），会抛出异常，
 * 各实例可能继续使用旧的模板缓存，直到该模板下一次保存、删除成功，或者重启实例
 *
 * @author 芋道源码
 */
@Service
@Validated
public class UReport2FileServiceImpl implements UReport2FileService {

    @Resource
    private UReport2FileMapper ureport2FileMapper;

    @Resource
    private UReport2FileVersionRedisDAO ureport2FileVersionRedisDAO;

    /**
     * 模板缓存；关闭 UReport2 时为 null
     */
    @Autowired(required = false) // 由于 ureport.enabled 配置项，可以关闭 UReport2Configuration 的功能，所以这里只能不强制注入
    private UReport2ReportDefinitionCache reportDefinitionCache;

    @Override
    public String saveFile(UReport2FileSaveReqVO saveReqVO) {
        Long tenantId = getTenantId();
        String name = UReport2FileNameUtils.normalize(saveReqVO.getName());
        UReport2FileDO file = ureport2FileMapper.selectByName(name);
        if (file == null) {
            // 情况一：不存在，则创建
            ureport2FileMapper.insert(new UReport2FileDO().setName(name).setContent(saveReqVO.getContent()));
        } else {
            // 情况二：已存在，则更新内容
            ureport2FileMapper.updateById(new UReport2FileDO().setId(file.getId()).setContent(saveReqVO.getContent()));
        }
        // 数据库已提交，发布新的版本号
        return publishVersion(tenantId, name);
    }

    @Override
    public void deleteFileByName(String fileName) {
        UReport2FileDO file = ureport2FileMapper.selectByName(UReport2FileNameUtils.normalize(fileName));
        if (file == null) {
            throw exception(UREPORT2_FILE_NOT_EXISTS);
        }
        deleteFile(file);
    }

    @Override
    public void deleteFile(Long id) {
        UReport2FileDO file = ureport2FileMapper.selectById(id);
        if (file == null) {
            throw exception(UREPORT2_FILE_NOT_EXISTS);
        }
        deleteFile(file);
    }

    private void deleteFile(UReport2FileDO file) {
        // 逻辑删除，删除后可以重新创建同名模板
        ureport2FileMapper.deleteById(file.getId());
        // 旧数据的名称可能带有尾部空格，所以先规范化
        String name = UReport2FileNameUtils.normalize(file.getName());
        try {
            // 数据库已提交，发布新的版本号，避免各实例继续预览已删除的模板
            publishVersion(getTenantId(), name);
        } finally {
            // 移除本实例的模板缓存（没有过期时间），即使发布版本号失败，也不再保留已删除模板的缓存
            if (reportDefinitionCache != null) {
                reportDefinitionCache.onFileDeleted(UReport2DatabaseReportProvider.PREFIX + name);
            }
        }
    }

    @Override
    public UReport2FileDO getFileByName(String name) {
        return ureport2FileMapper.selectByName(UReport2FileNameUtils.normalize(name));
    }

    @Override
    public List<UReport2FileDO> getFileList() {
        return ureport2FileMapper.selectListWithoutContent();
    }

    @Override
    public PageResult<UReport2FileDO> getFilePage(UReport2FilePageReqVO pageReqVO) {
        return ureport2FileMapper.selectPage(pageReqVO);
    }

    /**
     * 发布模板的新版本号
     *
     * @param tenantId 租户编号
     * @param name 模板名称，不包含存储前缀
     * @return 新的版本号
     */
    private String publishVersion(Long tenantId, String name) {
        // 版本号与 UReport2 的模板缓存共用，按包含存储前缀的模板名称存储
        return ureport2FileVersionRedisDAO.refresh(tenantId, UReport2DatabaseReportProvider.PREFIX + name);
    }

    private static Long getTenantId() {
        return ObjUtil.defaultIfNull(TenantContextHolder.getTenantId(), TenantUtils.GLOBAL_TENANT_ID);
    }

}
