package cn.iocoder.yudao.adminserver.modules.activiti.service.config;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import org.activiti.api.runtime.shared.security.PrincipalGroupsProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Service
public class UserGroupsProvider implements PrincipalGroupsProvider {

    @Override
    public List<String> getGroups(Principal principal) {

        if(principal instanceof Authentication){
            Authentication authentication = (Authentication) principal;
            final Object user = authentication.getPrincipal();
            if(  user instanceof LoginUser){
                return ((LoginUser) user).getGroups();
            }else{
                return Collections.emptyList();
            }
        }else{
            return Collections.emptyList();
        }

    }
}
