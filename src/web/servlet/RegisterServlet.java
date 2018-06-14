package web.servlet;

import domain.User;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import service.UserService;
import utils.CommonsUtils;
import utils.MailUtils;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Map<String, String[]> parameterMap = request.getParameterMap();
        User user = new User();
        try {
            //指定日期转换器(String-->Date)
            ConvertUtils.register(new Converter() {
                @Override
                public Object convert(Class aClass, Object o) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = simpleDateFormat.parse(o.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return date;
                }
            }, Date.class);

            BeanUtils.populate(user, parameterMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        user.setUid(CommonsUtils.getUUID());
        user.setTelephone(null);
        user.setState(0); //未激活
        user.setCode(CommonsUtils.getUUID());

        UserService userService = new UserService();
        boolean isRegisterSuccess = false;
        try {
            isRegisterSuccess = userService.register(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isRegisterSuccess) {
            //发送激活邮件
            String mailMsg = String.format("恭喜您注册成功，请点击链接进行激活" +
                    "<a href='http://localhost:8080/WebShop/active?activeCode=%s'>" +
                    "http://localhost:8080/WebShop/active?activeCode=%s</a>", user.getCode(), user.getCode());

            try {
                MailUtils.sendMail(user.getEmail(), mailMsg);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            response.sendRedirect(request.getContextPath() + "/registerSuccess.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/registerFail.jsp");
        }
    }
}
