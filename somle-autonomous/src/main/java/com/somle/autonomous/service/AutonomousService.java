package com.somle.autonomous.service;

import com.somle.autonomous.model.AutonomousAccount;
import com.somle.autonomous.model.AutonomousAuthToken;
import com.somle.autonomous.repository.AutonomousAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AutonomousService {
    private static final String BASEURL = "https://api-vendor.autonomous.ai";
    private final AutonomousAccountRepository repository;


    @Autowired
    public AutonomousService(AutonomousAccountRepository autonomousAccountRepository) {
        this.repository = autonomousAccountRepository;
    }

    /**
     * 更新或获取给定自动化账户的访问令牌。
     * 假定如有需要外部应处理令牌刷新。
     * 10分钟刷新所有账户的accessToken
     */
    @Transactional
//    @Scheduled(cron = "0 */10 * * * *")
    public void refreshOrObtainAccessToken() {
        List<AutonomousAccount> list = repository.findAll();
        if (list.isEmpty()) {
            log.warn("对应账户数量为0");
        } else {
            log.debug("账户数量 = {}", list.size());
            list.forEach(account -> {
                try {
                    AutonomousClient client = new AutonomousClient(account);
                    AutonomousAuthToken newToken = client.getAutonomousAuthTokenBySignIn(account);

                    repository.save(account.setAutonomousAuthToken(newToken));
                } catch (Exception e) {
                    // 考虑记录异常或根据您的业务需求处理它
                    throw new RuntimeException("更新访问令牌失败: " + e.getMessage(), e);
                }
            });

        }

    }


}
