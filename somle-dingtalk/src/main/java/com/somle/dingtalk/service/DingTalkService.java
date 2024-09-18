package com.somle.dingtalk.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiUserListidRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiUserListidResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.framework.common.util.json.JSONObject;
import com.somle.framework.common.util.json.JsonUtils;
import jakarta.annotation.PostConstruct;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.integration.support.MessageBuilder;
//import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.somle.dingtalk.model.DingTalkDepartment;
// import com.somle.model.DingTalkDepartmentMap.DingTalkDepartment;
// import com.somle.model.DingTalkDepartmentMap;
import com.somle.dingtalk.model.DingTalkResponse;
import com.somle.dingtalk.model.DingTalkToken;
import com.somle.dingtalk.repository.DingTalkTokenRepository;
import com.somle.framework.common.util.web.WebUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DingTalkService {
    private DingTalkToken token;
    private final String host = "https://api.dingtalk.com";
    private final String baseHost = "https://oapi.dingtalk.com";



//    @Autowired
//    MessageChannel departmentChannel;

    @Autowired
    DingTalkTokenRepository tokenRepository;

    @Scheduled(cron = "0 0 0 * * *") // Executes at 00:00 AM every day
    @PostConstruct
    private void init() {
        token = refreshAuth();
        log.debug("token set");
    }


    // public String getAccessToken() {
    //     String url = "https://oapi.dingtalk.com/gettoken?appkey=" + appKey + "&appsecret=" + appSecret;
    //     String response = restTemplate.getForObject(url, String.class);
    //     log.debug(response.toString());

    //     JSONObject jsonObject = JsonUtils.parseObject(response);
    //     return jsonObject.getString("access_token");
    // }

    public DingTalkToken refreshAuth() {
        log.info("fetching access token");
        String url = host + "/v1.0/oauth2/accessToken";
        var payload = JsonUtils.newObject();
        DingTalkToken token = tokenRepository.findAll().get(0);
        payload.put("appKey", token.getAppKey());
        payload.put("appSecret", token.getAppSecret());
        String accessToken = WebUtils.postRequest(url, Map.of(), Map.of(), payload, JSONObject.class).getString("accessToken");
        token.setAccessToken(accessToken);
        return token;
    }

    @SneakyThrows
    public DingTalkDepartment getDepartment(Long deptId) {
        log.info("fetching department detail for " + deptId);
        String endUrl = "/topapi/v2/department/get";

        Map<String, String> params = Map.of(
            "access_token", token.getAccessToken()
        );
        Map<String, String> headers = Map.of(
        );
        var payload = JsonUtils.newObject();
        payload.put("dept_id", deptId);
        DingTalkResponse response = WebUtils.postRequest(baseHost + endUrl, params, headers, payload, DingTalkResponse.class);
        log.debug(response.toString());
        return response.getResult(DingTalkDepartment.class);

//        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/get");
//        OapiV2DepartmentGetRequest req = new OapiV2DepartmentGetRequest();
//        req.setDeptId(deptId);
//        req.setLanguage("zh_CN");
//        OapiV2DepartmentGetResponse rsp = client.execute(req, token.getAccessToken());
//        return rsp.getResult();

    }

    public Stream<DingTalkDepartment> getDepartmentPath(long deptId) {
        DingTalkDepartment current = getDepartment(deptId);
        return Stream.concat(Stream.of(current), getDepartmentPath(current.getParentId()));
    }

    public DingTalkDepartment getParent(long deptId, Integer parentLevel) {
        List<DingTalkDepartment> path = getDepartmentPath(deptId).toList();
        return path.get(path.size() - 2);
    }

    // public DingTalkDepartmentMap getDepartmentMap(DingTalkDepartmentMap depts, DingTalkDepartment parent) {
    //     getChilds(parent).forEach(child->{
    //         depts.getMap().put(child.getDeptId(), child);
    //     });
    // }

    // public DingTalkDepartmentMap getDepartmentMap() {
    //     DingTalkDepartmentMap depts = new DingTalkDepartmentMap();
    //     return getDepartmentMap(depts, depts.getRoot());
    // }

    //return dept with null level
    public Stream<DingTalkDepartment> getOrphans(long deptId) {
        log.info("fetching department child for " + deptId);
        String endUrl = "/topapi/v2/department/listsub";

        Map<String, String> params = Map.of(
            "access_token", token.getAccessToken()
        );
        Map<String, String> headers = Map.of(
        );
        var payload = JsonUtils.newObject();
        payload.put("dept_id", deptId);
        DingTalkResponse response = WebUtils.postRequest(baseHost + endUrl, params, headers, payload, DingTalkResponse.class);
        log.debug(response.toString());
        return response.getResultList(DingTalkDepartment.class).stream();
    }

    //accept/return dept with non null level
    public Stream<DingTalkDepartment> getChilds(DingTalkDepartment dept) {
        // List<DingTalkDepartment> childPath = dept.getPath();
        // childPath.addLast(dept);
        return getOrphans(dept.getDeptId())
            .map(n->{
                n.setLevel(dept.getLevel() + 1);
                n.setParent(dept);
                return n;
            });
    }

    public DingTalkDepartment cleanDepartment(DingTalkDepartment dept) {
        String name = dept.getName();
        // Map<String, String> replacements = new HashMap<>();
        // replacements.put("创意", "CBD");
        // replacements.put("家居", "HCD");
        // replacements.put("办公", "EBD");
        // replacements.put("视听", "ABD");





        // boolean replaced = false;

        // // Check if the name starts with any key in the map
        // for (String key : replacements.keySet()) {
        //     if (name.startsWith(key)) {
        //         name = replacements.get(key) + name.substring(key.length());
        //         replaced = true;
        //         break;
        //     }
        // }

        // If the name starts with a non-ASCII character and was not replaced
        // char ch = name.charAt(0);
        // if (!replaced && !(ch >= 0 && ch <= 127)) {
        //     name = "$" + name;
        // }
        
        // if (name.startsWith("$") && name.contains("仓")) {
        //     name = "LC" + name.substring(1);
        // } else if (name.startsWith("$")) {
        //     name = "MC" + name.substring(1);
        // }

        dept.setName(name);

        return dept;

    }

    public DingTalkResponse addDepartment(DingTalkDepartment dept) {
        log.info("adding departments");
        String endUrl = "/topapi/v2/department/update";
        
        Map<String, String> params = Map.of(
            "access_token", token.getAccessToken()
        );
        Map<String, String> headers = Map.of(
        );
        DingTalkResponse response = WebUtils.postRequest(baseHost + endUrl, params, headers, dept, DingTalkResponse.class);
        return response;
    }


    public boolean cleanDepartments() {
        log.info("cleaning departments");

        
        return getDepartmentStream().map( dept -> {
            DingTalkDepartment newDept = cleanDepartment(dept);
            DingTalkResponse response = addDepartment(newDept);
            boolean success = response.getErrcode() == 200;
            if (success) {
                log.debug(newDept.getName() + " cleaned successfully");
            } else {
                log.error(newDept.getName() + " clean failed\n" + response.getErrmsg());
            }
            return success;
        })
        .reduce(true, (a, b) -> a && b);
    }
    
    public Stream<DingTalkDepartment> toStream(DingTalkDepartment dept) {
        // List<String> keys = List.of("CBD", "HCD", "EBD", "ABD", "DCD", "VC", "QC", "MC", "FC", "HC", "CC", "PC", "LC");
        List<DingTalkDepartment> childList = getChilds(dept).toList();
        if ( childList.isEmpty()) {
            log.debug("null child list");
            return Stream.of();
        } else {
            log.debug("non null child list");
            log.debug(childList.toString());
            return Stream.concat(childList.stream(), childList.stream().flatMap(n -> toStream(n)));
            // return Stream.concat(childList.stream().filter(n->! keys.stream().anyMatch(key->n.getName().startsWith(key))), childList.stream().flatMap(n -> getDepartmentsResursive(n)));
        }
    }

    public Stream<DingTalkDepartment> getDepartmentStream() {
        return toStream(
            DingTalkDepartment.builder()
                .deptId(1l)
                .level(0)
                .build()
        );
    }

    




//    public void uploadDepartmentsResursive() {
//        getDepartmentStream()
//            // .limit(4)
//            .forEach(n -> {
//                departmentChannel.send(MessageBuilder.withPayload(n).build());
//                // departmentChannel.send(MessageBuilder.withPayload(n).build());
//            });
//    }

    @SneakyThrows
    public List<String> getUserIds(Long deptId) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listid");
        OapiUserListidRequest req = new OapiUserListidRequest();
        req.setDeptId(deptId);
        OapiUserListidResponse rsp = client.execute(req, token.getAccessToken());
        return rsp.getResult().getUseridList();
    }

    @SneakyThrows
    public OapiV2UserGetResponse.UserGetResponse getUserDetail(String userId) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid(userId);
        req.setLanguage("zh_CN");
        OapiV2UserGetResponse rsp = client.execute(req, token.getAccessToken());
        return rsp.getResult();
    }

    public Stream<OapiV2UserGetResponse.UserGetResponse> getUserDetailStream() {
        return getDepartmentStream()
            .map(DingTalkDepartment::getDeptId)
            .map(this::getUserIds)
            .flatMap(Collection::stream)
            .map(this::getUserDetail);
    }

    
}
