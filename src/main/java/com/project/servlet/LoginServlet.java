package com.project.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.project.db.DB;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Get user input
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String role = req.getParameter("role");

        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, name, role FROM Users WHERE email = ? AND password = ? AND role = ?")) {

            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, role);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                // Wrong credentials
                resp.sendRedirect(req.getContextPath() + "/login.html?error=1");
                return;
            }

            int userId = rs.getInt("id");
            String name = rs.getString("name");
            String userRole = rs.getString("role");

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", userId);
            session.setAttribute("name", name);
            session.setAttribute("role", userRole); // Store user info

            if ("INSTITUTE".equalsIgnoreCase(userRole)) {
                resp.sendRedirect(req.getContextPath() + "/institutionDashboard.html?msg=welcome");
            } else {
                resp.sendRedirect(req.getContextPath() + "/freelancerDashboard.html?msg=welcome");
            }

        } catch (SQLException e) {
            throw new ServletException("Database access error during login", e);
        }
    }
}
