package com.project.servlet;

import com.project.db.DB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Handles user registration
@WebServlet("/api/signup")
public class SignupServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Get form data
        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String role = req.getParameter("role");

        try (Connection conn = DB.getConnection()) {

            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM Users WHERE email = ?")) {
                check.setString(1, email);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    // Email already exists
                    resp.sendRedirect(req.getContextPath() + "/signup.html?error=1");
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Users(name, email, password, role) VALUES (?,?,?,?)")) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.setString(4, role);
                ps.executeUpdate(); // Save user
            }

            resp.sendRedirect(req.getContextPath() + "/login.html?msg=signup_success");

        } catch (SQLException e) {
            throw new ServletException("Database error during signup", e);
        }
    }
}
