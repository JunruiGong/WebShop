package dao;

import domain.Category;
import domain.Product;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import utils.JdbcUtilsConfig;

import java.sql.SQLException;
import java.util.List;

public class ProductDao {
    public List<Product> getHotProductList() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product where is_hot=1 limit 0,9";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class));
    }

    public List<Product> getNewProductList() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product order by pdate desc limit 0,9";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class));
    }

    public List<Category> getAllCategory() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from category";
        return queryRunner.query(sql, new BeanListHandler<Category>(Category.class));
    }


    public int getCount(String cid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select count(*) from product where cid=?";
        Long query = (Long) queryRunner.query(sql, new ScalarHandler(), cid);
        return query.intValue();
    }

    public List<Product> findProductByPage(String cid, int index, int currentCount) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product where cid=? limit ?,?";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class), cid, index, currentCount);
    }

    public Product getProductInfoByPid(String pid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product where pid=?";
        return queryRunner.query(sql, new BeanHandler<Product>(Product.class), pid);
    }
}
