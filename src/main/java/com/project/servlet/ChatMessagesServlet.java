package com.project.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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

// Fetch chat history
@WebServlet("/api/chat/messages")
public class ChatMessagesServlet extends HttpServlet {

    // Get messages for a chat
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String convIdStr = req.getParameter("conversationId");
        if (convIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int conversationId = Integer.parseInt(convIdStr);

        ChatStorage.Conversation conv = ChatStorage.getConversation(conversationId);
        if (conv == null || (userId != conv.instituteId && userId != conv.freelancerId)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.setContentType("application/json;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            ChatStorage.markAsRead(conversationId, userId);

            List<ChatStorage.Message> msgs = ChatStorage.getMessages(conversationId);
            JSONArray arr = new JSONArray();
            for (ChatStorage.Message m : msgs) {
                JSONObject o = new JSONObject();
                o.put("me", m.senderId == userId);
                o.put("body", m.body);
                arr.put(o);
            }
            out.write(arr.toString());
        }
    }
}

