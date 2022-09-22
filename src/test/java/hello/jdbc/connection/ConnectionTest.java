package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    //그냥 driverManager
    @Test
    void driverManger() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        //실제 커넥션 2개 만듦

        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());

    }

    //DataSource 인터페이스를 구현한 DriverManagerDataSource 사용
    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 이것도 DataSource 인터페이스를 구현하긴 하지만 내부적으로 항상 Drivermanager 을 쓴다.
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);
    }

    //커넥션 풀에서 커넥션 가져오기.
    //DataSource 인터페이스 중 HikariDataSource 사용
    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        //커넥션 pulling(커넥션 풀에서 가져옴.)
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(1); //default가 10개이다.
        dataSource.setPoolName("myPool");

        useDataSource(dataSource);
        Thread.sleep(1000); // 커넥션 풀에서 커넥션이 생성되기를 대기.
    }

    //로그 찍기용.
    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}
