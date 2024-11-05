package com.somle.esb.controller;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import com.somle.esb.service.EsbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/esb")
public class EsbController {

    @Autowired
    private EsbService service;

    @PostMapping("/getBeans")
    public void printAllBeans() {
        service.printAllBeans();
    }



//    @PostMapping("/dataCollect")
//    public String dataCollect(LocalDate scheduleDate, String database) {
//        if (database == null) {
//            service.dataCollect(scheduleDate);
//        } else {
//            service.dataCollect(scheduleDate, Domain.fromString(database));
//        }
//        return "success";
//    }



    @PostMapping("/syncUsers")
    public String syncUsers() {
        /*ErpProductDTO a = new ErpProductDTO();
        a.setProductDeptId(50007L);
        service.handleProductsToKingdee(MessageBuilder.withPayload(List.of(a)).build());*/
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