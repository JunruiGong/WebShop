package web.servlet;

import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "CheckUsernameServlet", urlPatterns = {"/checkUsername"})
public class CheckUsernameServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        UserService userService = new UserService();
        boolean isExist = false;
        try {
             isExist = userService.checkUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json="{\"isExist\":"+isExist+"}";
        response.getWriter().write(json);
        System.out.println(isExist);

    }
}
