package cn.iocoder.dashboard.modules.system.service.auth.impl;

import cn.iocoder.dashboard.framework.security.config.SecurityProperties;
import cn.iocoder.dashboard.modules.system.service.auth.SysTokenService;
import cn.iocoder.dashboard.util.date.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Token Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class SysTokenServiceImpl implements SysTokenService {

    @Resource
    private SecurityProperties securityProperties;

    @Override
    public String createToken(String subject) {
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, securityProperties.getTokenSecret())
                .setExpiration(DateUtils.addTime(securityProperties.getTokenTimeout()))
                .setSubject(subject)
                .compact();
    }

    @Override
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(securityProperties.getTokenSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    public static void main(String[] args) {
        String secret = "abcdefghijklmnopqrstuvwxyz";
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        System.out.println(Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, secret)
                .setClaims(map)
                .compact());

        System.out.println(Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws("qyJhbGciOiJIUzUxMiJ9.eyJrZXkxIjoidmFsdWUxIn0.AHWncLRBlJkqrKaoWHZmMgbqYIT7rfLs8KCp9LuC0mdNfnx1xEMm1N9bgcD-0lc5sjySqsKiWzqJ3rpoyUSh0g")
                .getBody());
    }

}
