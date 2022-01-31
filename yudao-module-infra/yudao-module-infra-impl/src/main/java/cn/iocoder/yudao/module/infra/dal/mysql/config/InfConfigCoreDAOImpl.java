package cn.iocoder.yudao.module.infra.dal.mysql.config;

import cn.iocoder.yudao.framework.apollo.internals.ConfigFrameworkDAO;
import cn.iocoder.yudao.framework.apollo.internals.dto.ConfigRespDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

/**
 * ConfigFrameworkDAO Core 实现类
 *
 * @author 芋道源码
 */
public class InfConfigCoreDAOImpl implements ConfigFrameworkDAO {

    private final JdbcTemplate jdbcTemplate;

    public InfConfigCoreDAOImpl(String jdbcUrl, String username, String password) {
        DataSource dataSource = new DriverManagerDataSource(jdbcUrl, username, password);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime) {
        return jdbcTemplate.query("SELECT id FROM inf_config WHERE update_time > ? LIMIT 1",
                ResultSet::next, maxUpdateTime);
    }

    @Override
    public List<ConfigRespDTO> selectList() {
        return jdbcTemplate.query("SELECT `key`, `value`, update_time, deleted FROM inf_config", new BeanPropertyRowMapper<>(ConfigRespDTO.class));
    }

}
