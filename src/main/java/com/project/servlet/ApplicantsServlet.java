package com.project.servlet;

import com.project.db.DB;
import org.json.JSONArray;
import org.json.JSONObject;

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

// List applicants for a job
@WebServlet("/api/applicants")
public class ApplicantsServlet extends HttpServlet {

    // Get applicants
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || !"INSTITUTE".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int instituteId = (Integer) session.getAttribute("userId");
        String jobIdStr = req.getParameter("jobId");
        if (jobIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int jobId = Integer.parseInt(jobIdStr);

        resp.setContentType("application/json;charset=UTF-8");

        try (Connection conn = DB.getConnection()) {
            // Check if user owns the job
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT id FROM Jobs WHERE id = ? AND institute_id = ?")) {
                ps.setInt(1, jobId);
                ps.setInt(2, instituteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }
            }

            // Fetch Applicants
            String sql = "SELECT u.id, u.name, u.email, ja.created_at FROM JobApplications ja JOIN Users u ON ja.jobseeker_id = u.id WHERE ja.job_id = ? ORDER BY ja.created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, jobId);
                try (ResultSet rs = ps.executeQuery()) {
                    JSONArray list = new JSONArray();
                    while (rs.next()) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", rs.getInt("id"));
                        obj.put("name", rs.getString("name"));
                        obj.put("email", rs.getString("email"));
                        obj.put("appliedAt", rs.getTimestamp("created_at").toString());
                        list.put(obj);
                    }
                    try (PrintWriter out = resp.getWriter()) {
                        out.write(list.toString());
                    }
                }
            }

        } catch (SQLException e) {
            throw new ServletException("Error retrieving applicants", e);
        }
    }
}
