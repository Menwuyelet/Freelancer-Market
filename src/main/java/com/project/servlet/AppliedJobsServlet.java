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

// Show jobs user applied to
@WebServlet("/api/applied-jobs")
public class AppliedJobsServlet extends HttpServlet {

    private static class AppliedJob {
        int id;
        String title;
        String content;
        String budget;
        String instituteName;

        AppliedJob(int id, String title, String content, String budget, String instituteName) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.budget = budget;
            this.instituteName = instituteName;
        }
    }

    // Get application history
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"JOB_SEEKER".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int jobseekerId = (Integer) session.getAttribute("userId");
        resp.setContentType("application/json;charset=UTF-8");
        List<AppliedJob> appliedJobs = new ArrayList<>();
        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT a.id, a.title, a.content, a.budget, u.name FROM JobApplications app JOIN Jobs a ON app.job_id = a.id LEFT JOIN Users u ON a.institute_id = u.id WHERE app.jobseeker_id = ? ORDER BY app.created_at DESC");) {
            ps.setInt(1, jobseekerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String instituteName = rs.getString(5);
                    if (instituteName == null)
                        instituteName = "Unknown";
                    appliedJobs.add(new AppliedJob(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            instituteName));
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
        for (AppliedJob aj : appliedJobs) {
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("id", aj.id);
            obj.put("title", aj.title);
            obj.put("content", aj.content);
            obj.put("budget", aj.budget != null ? aj.budget : "");
            obj.put("instituteName", aj.instituteName);
            jsonArray.put(obj);
        }

        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonArray.toString());
        }
    }
}