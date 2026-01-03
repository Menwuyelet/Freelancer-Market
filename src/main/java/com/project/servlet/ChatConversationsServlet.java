package com.project.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.project.db.ChatStorage;
import com.project.db.DB;

// API for chat conversations
@WebServlet("/api/chat/conversations")
public class ChatConversationsServlet extends HttpServlet {

    // List user conversations
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        resp.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            List<ChatStorage.Conversation> convs = ChatStorage.getConversationsForUser(userId, role);
            JSONArray arr = new JSONArray();
            for (ChatStorage.Conversation conv : convs) {
                JSONObject o = new JSONObject();
                int otherId = "INSTITUTE".equalsIgnoreCase(role) ? conv.freelancerId : conv.instituteId;
                String otherName = DB.getUserName(otherId);
                o.put("id", conv.id);
                o.put("otherName", otherName);
                o.put("otherRole", "INSTITUTE".equalsIgnoreCase(role) ? "Freelancer" : "Institution");
                arr.put(o);
            }
            out.write(arr.toString());
        } catch (SQLException e) {
            throw new ServletException("Error retrieving conversations", e);
        }
    }

    // Create new conversation
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String otherIdStr = req.getParameter("otherUserId");
        if (otherIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int otherId = Integer.parseInt(otherIdStr);

        int instituteId = "INSTITUTE".equalsIgnoreCase(role) ? userId : otherId;
        int freelancerId = "JOB_SEEKER".equalsIgnoreCase(role) ? userId : otherId;

        int convId = ChatStorage.createConversation(instituteId, freelancerId);
        resp.setContentType("application/json");
        try (PrintWriter out = resp.getWriter()) {
            JSONObject o = new JSONObject();
            o.put("conversationId", convId);
            out.write(o.toString());
        }
    }
}

