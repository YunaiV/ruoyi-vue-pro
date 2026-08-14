package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.file.HrmEmployeeFileSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeFileDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeFileMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_FILE_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_FILE_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_TYPE;

/**
 * HRM 员工材料附件 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeFileServiceImpl implements HrmEmployeeFileService {

    @Resource
    private HrmEmployeeFileMapper employeeFileMapper;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_FILE_UPDATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_FILE_UPDATE_SUCCESS)
    public void saveEmployeeFiles(HrmEmployeeFileSaveReqVO reqVO) {
        // 1. 校验员工
        HrmEmployeeDO employee = employeeService.validateEmployeeExists(reqVO.getEmployeeId());

        // 2.1 按 URL 计算材料附件差异
        List<HrmEmployeeFileDO> dbFiles = employeeFileMapper.selectListByEmployeeIdAndType(
                reqVO.getEmployeeId(), reqVO.getType());
        Set<String> fileUrls = new LinkedHashSet<>(reqVO.getFileUrls());
        List<HrmEmployeeFileDO> newFiles = convertList(fileUrls, fileUrl -> new HrmEmployeeFileDO()
                .setEmployeeId(reqVO.getEmployeeId()).setType(reqVO.getType()).setUrl(fileUrl));
        List<List<HrmEmployeeFileDO>> diffFiles = diffList(dbFiles, newFiles,
                (oldFile, newFile) -> oldFile.getUrl().equals(newFile.getUrl()));
        // 2.2 新增请求中增加的附件
        if (CollUtil.isNotEmpty(diffFiles.get(0))) {
            employeeFileMapper.insertBatch(diffFiles.get(0));
        }
        // 2.3 删除请求中移除的附件及历史重复附件
        if (CollUtil.isNotEmpty(diffFiles.get(2))) {
            employeeFileMapper.deleteByIds(convertList(diffFiles.get(2), HrmEmployeeFileDO::getId));
        }

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    public List<HrmEmployeeFileDO> getEmployeeFileList(Long employeeId) {
        return employeeFileMapper.selectListByEmployeeId(employeeId);
    }

}
