package com.somle.autonomous.service;

import com.somle.autonomous.model.AutonomousAuthToken;
import com.somle.autonomous.repository.AutonomousAccountRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AutonomousService {

    @Resource
    public AutonomousAccountRepository repository;

    public List<AutonomousClient> autonomousClients;

    @PostConstruct
    public void init() {
        autonomousClients = repository.findAll().stream()
            .map(AutonomousClient::new)
            .toList();
    }

    /**
     * 更新或获取给定自动化账户的访问令牌。
     * 假定如有需要外部应处理令牌刷新。
     * 10分钟刷新所有账户的accessToken
     */
//    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void refreshOrObtainAccessToken() {
        if (autonomousClients.isEmpty()) {
            log.warn("对应账户数量为0");
        } else {
            log.debug("账户数量 = {}", autonomousClients.size());
            autonomousClients.forEach(client -> {
                AutonomousAuthToken newToken = client.getAutonomousAuthTokenBySignIn(client.autonomousAccount);
                repository.save(client.autonomousAccount.setAutonomousAuthToken(newToken));
            });
        }

    }

}
