package utils;


/**
 * 采用连接池、读取配置文件方式使用JDBC。
 */

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtilsConfig {

    private static BasicDataSource dataSource = new BasicDataSource();
    private static String driverClass;
    private static String url;
    private static String username;
    private static String password;

    static {
        readConfig();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setInitialSize(10);
        dataSource.setMaxActive(8);
        dataSource.setMaxIdle(5);
        dataSource.setMinIdle(1);

        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setTestWhileIdle(true);
    }

    private static void readConfig() {
        try {
            InputStream inputStream = JdbcUtilsConfig.class.getResourceAsStream("database.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            driverClass = properties.getProperty("driverClass");
            url = properties.getProperty("url");

            username = properties.getProperty("username");
            password = properties.getProperty("password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }


}
