package web.servlet;

import com.google.gson.Gson;
import domain.Cart;
import domain.CartItem;
import domain.Category;
import domain.Product;
import redis.clients.jedis.Jedis;
import service.ProductService;
import utils.JedisPoolUtils;
import vo.PageBean;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "ProductServlet", urlPatterns = {"/product"})
public class ProductServlet extends BaseServlet {

    //显示商品的分类
    public void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductService productService = new ProductService();

        //先从缓存中查询category，如果没有再从数据库中查询，存在缓存中
        Jedis jedis = JedisPoolUtils.getJedis();
        String categoryListJson = jedis.get("categoryListJson");
        if (categoryListJson == null) {
            //商品分类
            List<Category> allCategoryList = null;
            try {
                allCategoryList = productService.getAllCategory();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Gson gson = new Gson();
            categoryListJson = gson.toJson(allCategoryList);
            jedis.set("categoryListJson", categoryListJson);

        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(categoryListJson);
    }

    //首页显示热门商品、最新商品
    public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ProductService productService = new ProductService();

        //热门商品
        List<Product> hotProductList = null;
        try {
            hotProductList = productService.getHotProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("hotProductList", hotProductList);

        //最新商品
        List<Product> newProductList = null;
        try {
            newProductList = productService.getNewProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("newProductList", newProductList);

        //商品分类
        List<Category> allCategoryList = null;
        try {
            allCategoryList = productService.getAllCategory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("allCategoryList", allCategoryList);

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    //显示商品的详细信息
    public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String currentPage = request.getParameter("currentPage");
        String cid = request.getParameter("cid");
        String pid = request.getParameter("pid");

        ProductService productService = new ProductService();
        Product product = null;
        try {
            product = productService.getProductInfoByPid(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("product", product);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("cid", cid);

        //获得客户端携带得cookie---pid(s)
        String pids = pid;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("pids".equals(cookie.getName())) {
                    pids = cookie.getValue();
                    String[] split = pids.split("-");
                    List<String> asList = Arrays.asList(split);
                    LinkedList<String> list = new LinkedList<String>(asList);
                    //判断集合中是否存在当前pid
                    if (list.contains(pid)) {
                        list.remove(pid);
                    }
                    list.addFirst(pid);

                    //将集合转成字符串
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0; i < list.size() && i < 7; i++) {
                        stringBuffer.append(list.get(i));
                        stringBuffer.append("-");
                    }
                    //去掉最后一个“-”
                    pids = stringBuffer.substring(0, stringBuffer.length() - 1);
                }
            }
        }

        Cookie newCookie = new Cookie("pids", pids);
        response.addCookie(newCookie);

        request.getRequestDispatcher("/product_info.jsp").forward(request, response);
    }


    //根据cid获得商品信息
    public void productListByCid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cid = request.getParameter("cid");
        ProductService productService = new ProductService();

        //模拟当前页是第一页
        String currentPageStr = request.getParameter("currentPage");
        if (currentPageStr == null) {
            currentPageStr = "1";
        }
        int currentPage = Integer.parseInt(currentPageStr);

        PageBean<Product> pageBean = null;
        try {
            pageBean = productService.findProductListByCid(currentPage, cid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("pageBean", pageBean);
        request.setAttribute("cid", cid);

        //定义一个集合用于记录历史商品信息
        List<Product> historyProductList = new ArrayList<>();

        //获得客户端携带的pids的cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("pids".equals(cookie.getName())) {
                    String pids = cookie.getValue();
                    String[] split = pids.split("-");
                    for (String pid : split) {
                        Product product = null;
                        try {
                            product = productService.getProductInfoByPid(pid);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        historyProductList.add(product);
                    }
                }
            }
        }
        request.setAttribute("historyProductList", historyProductList);

        request.getRequestDispatcher("/product_list.jsp").forward(request, response);
    }

    //将商品添加到购物车
    public void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        String pid = request.getParameter("pid");
        int quantity = Integer.parseInt(request.getParameter("quantity"));


        ProductService productService = new ProductService();
        Product product = null;
        try {
            product = productService.getProductInfoByPid(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double subtotal = quantity * product.getShop_price();

        //封装cartItem
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setSubtotal(subtotal);

        //封装购物车----先判断是否已经存在购物车
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }

        Map<String, CartItem> cartItems = cart.getCartItems();
        //判断该购物车中是否已经含有此商品
        if (cartItems.containsKey(pid)) {

            //修改数量
            CartItem cartItem1 = cartItems.get(pid);
            int oldQuantity = cartItem1.getQuantity();
            oldQuantity += quantity;
            cartItem1.setQuantity(oldQuantity);

            //修改小计
            cartItem1.setSubtotal(oldQuantity * cartItem1.getProduct().getShop_price());

        } else {
            //将商品放到购物车中
            cart.getCartItems().put(product.getPid(), cartItem);
        }

        //计算总金额
        double total = cart.getTotal() + subtotal;
        cart.setTotal(total);

        session.setAttribute("cart", cart);

        //直接跳转到购物车
        response.sendRedirect(request.getContextPath() + "/cart.jsp");
    }

    //删除购物车商品
    public void deleteProductFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String pid = request.getParameter("pid");
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {

            cart.setTotal(cart.getTotal() - cart.getCartItems().get(pid).getSubtotal());//修改总计
            cart.getCartItems().remove(pid);  //删除该商品
            session.setAttribute("cart", cart);
        }

        //直接跳转到购物车
        response.sendRedirect(request.getContextPath() + "/cart.jsp");
    }

    //清空购物车
    public void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("cart");
        response.sendRedirect(request.getContextPath() + "/cart.jsp");
    }

}
