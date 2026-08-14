package cn.iocoder.yudao.module.hrm.service.insurance.config;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard.HrmInsuranceStandardProjectRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.standard.HrmInsuranceStandardTypeRespVO;
import cn.iocoder.yudao.module.hrm.enums.insurance.config.HrmInsuranceProjectTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.hrm.dal.redis.RedisKeyConstants.INSURANCE_STANDARD_PROJECT;
import static cn.iocoder.yudao.module.hrm.dal.redis.RedisKeyConstants.INSURANCE_STANDARD_TYPE;

/**
 * HRM 标准参保数据 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class HrmInsuranceStandardServiceImpl implements HrmInsuranceStandardService {

    private static final String INSURANCE_TYPE_URL =
            "https://user.1renshi.com/Home/GetSocialTypeAndHouseRange";
    private static final String INSURANCE_SCALE_URL =
            "https://www.shebao100.cn/user/funbasequery";

    private static final int HTTP_TIMEOUT_MILLIS = 5000;

    /**
     * 获得指定地区的标准参保类型
     *
     * @param areaId 地区编号
     * @return 标准参保类型列表
     */
    @Override
    @Cacheable(cacheNames = INSURANCE_STANDARD_TYPE + "#7d", key = "#areaId",
            unless = "#result.isEmpty()")
    public List<HrmInsuranceStandardTypeRespVO> getStandardTypeList(Integer areaId) {
        String cityName = getCityName(areaId);
        if (StrUtil.isBlank(cityName)) {
            return Collections.emptyList();
        }
        try (HttpResponse response = HttpRequest.get(INSURANCE_TYPE_URL)
                .form(Collections.singletonMap("cityName", cityName))
                .timeout(HTTP_TIMEOUT_MILLIS)
                .execute()) {
            if (!response.isOk()) {
                log.warn("[getStandardTypeList][areaId({}) cityName({}) 查询失败，响应状态码为 {}]",
                        areaId, cityName, response.getStatus());
                return Collections.emptyList();
            }
            return parseTypeList(response.body());
        } catch (Exception ex) {
            log.warn("[getStandardTypeList][areaId({}) cityName({}) 查询失败]", areaId, cityName, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 获得指定地区和参保类型的标准参保项目
     *
     * @param areaId 地区编号
     * @param typeCode 参保类型编码
     * @return 标准参保项目列表
     */
    @Override
    @Cacheable(cacheNames = INSURANCE_STANDARD_PROJECT + "#7d", key = "#areaId + ':' + #typeCode",
            unless = "#result.isEmpty()")
    public List<HrmInsuranceStandardProjectRespVO> getStandardProjectList(Integer areaId, String typeCode) {
        String cityName = getCityName(areaId);
        if (StrUtil.isBlank(cityName) || StrUtil.isBlank(typeCode)) {
            return Collections.emptyList();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("js_cityid_name", cityName);
        params.put("cs_classtype", typeCode);
        params.put("X-Requested-With", "XMLHttpRequest");
        try (HttpResponse response = HttpRequest.get(INSURANCE_SCALE_URL)
                .header("X-Requested-With", "XMLHttpRequest")
                .form(params)
                .timeout(HTTP_TIMEOUT_MILLIS)
                .execute()) {
            if (!response.isOk()) {
                log.warn("[getStandardProjectList][areaId({}) cityName({}) typeCode({}) 查询失败，响应状态码为 {}]",
                        areaId, cityName, typeCode, response.getStatus());
                return Collections.emptyList();
            }
            return parseProjectList(response.body());
        } catch (Exception ex) {
            log.warn("[getStandardProjectList][areaId({}) cityName({}) typeCode({}) 查询失败]",
                    areaId, cityName, typeCode, ex);
            return Collections.emptyList();
        }
    }

    /**
     * 解析标准参保类型响应
     *
     * @param body 响应内容
     * @return 标准参保类型列表
     */
    List<HrmInsuranceStandardTypeRespVO> parseTypeList(String body) {
        JSONObject data = JSONUtil.parseObj(body).getJSONObject("data");
        JSONArray socialTypes = data == null ? null : data.getJSONArray("SocialTypes");
        if (socialTypes == null || socialTypes.isEmpty()) {
            return Collections.emptyList();
        }
        List<HrmInsuranceStandardTypeRespVO> result = new ArrayList<>(socialTypes.size());
        for (int i = 0; i < socialTypes.size(); i++) {
            JSONObject item = socialTypes.getJSONObject(i);
            String code = item.getStr("AccountNo");
            String name = item.getStr("InsuredType");
            if (StrUtil.isNotBlank(code) && StrUtil.isNotBlank(name)) {
                result.add(new HrmInsuranceStandardTypeRespVO().setCode(code).setName(name));
            }
        }
        return result;
    }

    /**
     * 解析标准参保项目响应
     *
     * @param body 响应内容
     * @return 标准参保项目列表
     */
    List<HrmInsuranceStandardProjectRespVO> parseProjectList(String body) {
        Map<Integer, HrmInsuranceStandardProjectRespVO> projectMap = new LinkedHashMap<>();
        for (Element row : Jsoup.parse(body).select("tr")) {
            Elements cells = row.children();
            if (cells.size() < 7 || ObjUtil.notEqual("td", cells.get(0).normalName())) {
                continue;
            }
            HrmInsuranceProjectTypeEnum projectType = getProjectType(cells.get(0).text());
            if (projectType == null) {
                continue;
            }
            projectMap.putIfAbsent(projectType.getType(), new HrmInsuranceStandardProjectRespVO()
                    .setType(projectType.getType()).setName(projectType.getName())
                    .setBaseAmount(NumberUtils.parseFirstBigDecimal(cells.get(1).text()))
                    .setCorporateRate(NumberUtils.parseFirstBigDecimal(cells.get(3).text()))
                    .setPersonalRate(NumberUtils.parseFirstBigDecimal(cells.get(4).text()))
                    .setCorporateAmount(NumberUtils.parseFirstBigDecimal(cells.get(5).text()))
                    .setPersonalAmount(NumberUtils.parseFirstBigDecimal(cells.get(6).text())));
        }
        return new ArrayList<>(projectMap.values());
    }

    private HrmInsuranceProjectTypeEnum getProjectType(String name) {
        if (name.contains("大病") || name.contains("大额医疗")
                || name.contains("补充") && name.contains("医疗")) {
            return HrmInsuranceProjectTypeEnum.SUPPLEMENTARY_MEDICAL_INSURANCE;
        }
        if (name.contains("补充") && name.contains("养老")) {
            return HrmInsuranceProjectTypeEnum.SUPPLEMENTARY_PENSION_INSURANCE;
        }
        if (name.contains("养老")) {
            return HrmInsuranceProjectTypeEnum.PENSION_INSURANCE;
        }
        if (name.contains("医疗")) {
            return HrmInsuranceProjectTypeEnum.MEDICAL_INSURANCE;
        }
        if (name.contains("失业")) {
            return HrmInsuranceProjectTypeEnum.UNEMPLOYMENT_INSURANCE;
        }
        if (name.contains("工伤")) {
            return HrmInsuranceProjectTypeEnum.WORK_INJURY_INSURANCE;
        }
        if (name.contains("生育")) {
            return HrmInsuranceProjectTypeEnum.MATERNITY_INSURANCE;
        }
        if (name.contains("残保")) {
            return HrmInsuranceProjectTypeEnum.DISABILITY_INSURANCE;
        }
        if (name.contains("公积金")) {
            return HrmInsuranceProjectTypeEnum.PROVIDENT_FUND;
        }
        return null;
    }

    /**
     * 获得标准参保数据使用的城市名称
     *
     * @param areaId 地区编号
     * @return 城市名称
     */
    static String getCityName(Integer areaId) {
        Integer cityAreaId = AreaUtils.getParentIdByType(areaId, AreaTypeEnum.CITY);
        Area area = AreaUtils.getArea(cityAreaId != null ? cityAreaId : areaId);
        if (area == null) {
            return null;
        }
        return StrUtil.removeSuffix(StrUtil.removeSuffix(area.getName(), "城区"), "市");
    }

}
