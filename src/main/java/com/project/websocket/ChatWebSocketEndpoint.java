package com.project.websocket;

import com.project.db.ChatStorage;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Real-time chat endpoint
@ServerEndpoint(value = "/ws/chat", configurator = HttpSessionConfigurator.class)
public class ChatWebSocketEndpoint {

    private static final Map<Integer, Session> sessions = Collections.synchronizedMap(new HashMap<>());

    // On connection
    @OnOpen
    public void onOpen(Session session) throws IOException {
        HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
        if (httpSession == null || httpSession.getAttribute("userId") == null) {
            session.close();
            return;
        }
        Integer userId = (Integer) httpSession.getAttribute("userId");
        sessions.put(userId, session);
        session.getUserProperties().put("userId", userId);
    }

    @OnClose
    public void onClose(Session session) {
        Object uid = session.getUserProperties().get("userId");
        if (uid instanceof Integer) {
            sessions.remove((Integer) uid);
        }
    }

    // Handle message
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        Object uid = session.getUserProperties().get("userId");
        if (!(uid instanceof Integer)) {
            session.close();
            return;
        }
        int senderId = (Integer) uid;

        JSONObject payload = new JSONObject(message);
        int conversationId = payload.getInt("conversationId");
        String body = payload.optString("body", "").trim();
        if (body.isEmpty()) {
            return;
        }

        ChatStorage.Conversation conv = ChatStorage.getConversation(conversationId);
        if (conv == null) {
            session.close();
            return;
        }

        // Ensure sender is part of the conversation
        if (senderId != conv.instituteId && senderId != conv.freelancerId) {
            session.close();
            return;
        }

        // Persist message
        try {
            ChatStorage.addMessage(conversationId, senderId, body);
        } catch (IllegalArgumentException e) {
            session.close();
            return;
        }

        // Broadcast to both participants if connected
        broadcastMessage(conversationId, senderId, body, conv.instituteId, conv.freelancerId);
    }

    // Notify clients
    private void broadcastMessage(int conversationId, int senderId, String body, int instituteId, int freelancerId) {
        JSONObject outbound = new JSONObject();
        outbound.put("type", "message");
        outbound.put("conversationId", conversationId);
        outbound.put("senderId", senderId);
        outbound.put("body", body);

        // send to institute
        sendIfOnline(instituteId, outbound);
        // send to freelancer
        sendIfOnline(freelancerId, outbound);
    }

    private void sendIfOnline(int userId, JSONObject outbound) {
        Session s = sessions.get(userId);
        if (s != null && s.isOpen()) {
            s.getAsyncRemote().sendText(outbound.toString());
        }
    }
}


