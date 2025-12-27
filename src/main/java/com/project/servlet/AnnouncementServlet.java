package com.project.servlet;

import com.project.db.DB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Manage job postings
@WebServlet("/api/announcements")
public class AnnouncementServlet extends HttpServlet {

    private static class Announcement {
        int id;
        String title;
        String content;
        String budget;
        int instituteId;
        String instituteName;

        Announcement(int id, String title, String content, String budget, int instituteId, String instituteName) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.budget = budget;
            this.instituteId = instituteId;
            this.instituteName = instituteName;
        }
    }

    // Get job list
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        List<Announcement> announcements = new ArrayList<>();
        String myJobs = req.getParameter("myJobs");
        String sql = "SELECT a.id, a.title, a.content, a.budget, a.institute_id, u.name FROM Jobs a LEFT JOIN Users u ON a.institute_id = u.id";
        if ("true".equals(myJobs)) {
            // Filter by logged-in institute
            HttpSession session = req.getSession(false);
            if (session == null || !"INSTITUTE".equals(session.getAttribute("role"))) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            sql += " WHERE a.institute_id = ?";
        }
        sql += " ORDER BY a.created_at DESC";
        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            if ("true".equals(myJobs)) {
                ps.setInt(1, (Integer) req.getSession().getAttribute("userId"));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String instituteName = rs.getString(6);
                    if (instituteName == null)
                        instituteName = "Unknown";
                    announcements.add(new Announcement(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getInt(5), instituteName));
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                org.json.JSONObject err = new org.json.JSONObject();
                err.put("error", "Database Error: " + e.getMessage());
                org.json.JSONArray errArray = new org.json.JSONArray();
                errArray.put(err);
                out.write(errArray.toString());
            }
            e.printStackTrace();
            return;
        }

        org.json.JSONArray jsonArray = new org.json.JSONArray();
        for (Announcement a : announcements) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", a.id);
            obj.put("title", a.title);
            obj.put("content", a.content);
            obj.put("budget", a.budget != null ? a.budget : "");
            obj.put("instituteId", a.instituteId);
            obj.put("instituteName", a.instituteName);
            jsonArray.put(obj);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonArray.toString());
        }
    }

    // Create new job
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null
                || !"INSTITUTE".equals(session.getAttribute("role"))) {
            // Not authorized
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int instituteId = (Integer) session.getAttribute("userId");
        String title = req.getParameter("title");
        String content = req.getParameter("content");
        String budget = req.getParameter("budget");

        if (title == null || content == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("INSERT INTO Jobs(title, content, budget, institute_id) VALUES (?,?,?,?)")) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, budget);
            ps.setInt(4, instituteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ServletException("Error creating job announcement", e);
        }

        resp.sendRedirect(req.getContextPath() + "/institutionDashboard.html?msg=job_posted");
    }
}