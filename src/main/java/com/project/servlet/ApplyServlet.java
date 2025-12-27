package com.project.servlet;

import com.project.db.DB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Job application logic
@WebServlet("/api/apply")
public class ApplyServlet extends HttpServlet {

    // Apply for job
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null
                || !"JOB_SEEKER".equals(session.getAttribute("role"))) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized");
            return;
        }

        int jobId;
        try {
            String jobIdStr = req.getParameter("jobId");
            if (jobIdStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Missing jobId");
                return;
            }
            jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid jobId");
            return;
        }

        int jobseekerId = (Integer) session.getAttribute("userId");

        try (Connection conn = DB.getConnection()) {
            // Check if already applied
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT id FROM JobApplications WHERE job_id = ? AND jobseeker_id = ?")) {
                ps.setInt(1, jobId);
                ps.setInt(2, jobseekerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        resp.getWriter().write("Already applied");
                        return;
                    }
                }
            }

            try (PreparedStatement ps = conn
                    .prepareStatement("INSERT INTO JobApplications(job_id, jobseeker_id) VALUES (?,?)")) {
                ps.setInt(1, jobId);
                ps.setInt(2, jobseekerId);
                ps.executeUpdate();
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("OK");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
