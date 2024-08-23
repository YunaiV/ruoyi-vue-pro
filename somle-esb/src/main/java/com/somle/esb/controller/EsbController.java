package com.somle.esb.controller;

import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/esb")
public class EsbController {

    @Autowired
    private EsbService esbService;

    @PostMapping("/dataCollect")
    public String dataCollect(LocalDate scheduleDate, String database) {
        if (database == null) {
            esbService.dataCollect(scheduleDate);
        } else {
            esbService.dataCollect(scheduleDate, Domain.fromString(database));
        }
        return "success";
    }


//    @RequestMapping(value = "ip")
//    public JSONObject getIP(HttpServletRequest request, HttpServletResponse response) {
//
//        String ip = request.getRemoteAddr();
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("HTTP_CLIENT_IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr();
//        }
//
//
//        //构建json
//        Map<String, Object> map = new HashMap();
//        map.put("IP", ip);
//        map.put("remote address", request.getRemoteAddr());
//        map.put("remote host", request.getRemoteHost());
//        map.put("remote user", request.getRemoteUser());
//        map.put("remote port", request.getRemotePort());
//        map.put("local address", request.getLocalAddr());
//        map.put("local name", request.getLocalName());
//        map.put("local port", request.getLocalPort());
//        JSONObject jsonObject = new JSONObject(map);
//        return jsonObject;
//    }
}