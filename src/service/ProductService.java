package service;

import dao.ProductDao;
import domain.Category;
import domain.Product;
import vo.PageBean;

import java.sql.SQLException;
import java.util.List;

public class ProductService {
    public List<Product> getHotProductList() throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getHotProductList();
    }

    public List<Product> getNewProductList() throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getNewProductList();
    }

    public List<Category> getAllCategory() throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getAllCategory();
    }


    public PageBean<Product> findProductListByCid(int currentPage, String cid) throws SQLException {
        PageBean<Product> pageBean = new PageBean<Product>();
        ProductDao productDao = new ProductDao();

        int currentCount = 12;

        pageBean.setCurrentPage(currentPage);
        pageBean.setCurrentCount(currentCount);

        int totalCount = productDao.getCount(cid);
        pageBean.setTotalCount(totalCount);

        int totalPage = (int) Math.ceil(1.0 * totalCount / currentCount);
        pageBean.setTotalPage(totalPage);

        int index = (currentPage - 1) * currentCount;
        List<Product> productList = productDao.findProductByPage(cid, index, currentCount);

        pageBean.setList(productList);

        return pageBean;
    }

    public Product getProductInfoByPid(String pid) throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getProductInfoByPid(pid);
    }

}
