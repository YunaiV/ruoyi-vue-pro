package cn.iocoder.yudao.module.crm.framework.excel.service;

import cn.iocoder.yudao.framework.excel.core.service.ExcelColumnSelectDataService;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Excel 所属地区列下拉数据源获取接口实现类
 *
 * @author HUIHUI
 */
@Service
public class AreaExcelColumnSelectDataServiceImpl implements ExcelColumnSelectDataService {

    public static final String FUNCTION_NAME = "getCrmAreaNameList"; // 防止和别的模块重名

    @Resource
    private DictDataApi dictDataApi;

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

    @Override
    public List<String> getSelectDataList() {
        // 获取地区下拉数据
        // TODO @puhui999：嘿嘿，这里改成省份、城市、区域，三个选项，难度大么？
        Area area = AreaUtils.parseArea(Area.ID_CHINA);
        return AreaUtils.getAreaNodePathList(area.getChildren());
    }

}
