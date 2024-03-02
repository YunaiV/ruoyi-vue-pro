package cn.iocoder.yudao.module.crm.framework.excel.service;

import cn.iocoder.yudao.framework.excel.core.service.ExcelColumnSelectDataService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_CUSTOMER_INDUSTRY;

/**
 * Excel 客户所属行业列下拉数据源获取接口实现类
 *
 * @author HUIHUI
 */
@Service
public class CrmCustomerIndustryExcelColumnSelectDataServiceImpl implements ExcelColumnSelectDataService {

    public static final String FUNCTION_NAME = "getCrmCustomerIndustryList";

    @Resource
    private DictDataApi dictDataApi;

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public List<String> getSelectDataList() {
        return dictDataApi.getDictDataLabelList(CRM_CUSTOMER_INDUSTRY);
    }

}
