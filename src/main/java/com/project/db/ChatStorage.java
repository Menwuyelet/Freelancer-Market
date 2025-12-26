package com.project.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatStorage {

    public static class Conversation {
        public int id;
        public int instituteId;
        public int freelancerId;
        public Timestamp createdAt;

        public Conversation(int id, int instituteId, int freelancerId, Timestamp createdAt) {
            this.id = id;
            this.instituteId = instituteId;
            this.freelancerId = freelancerId;
            this.createdAt = createdAt;
        }
    }

    public static class Message {
        public int id;
        public int senderId;
        public String body;
        public Timestamp createdAt;
        public boolean isRead;

        public Message(int id, int senderId, String body, Timestamp createdAt, boolean isRead) {
            this.id = id;
            this.senderId = senderId;
            this.body = body;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }
    }

    // List conversations
    public static List<Conversation> getConversationsForUser(int userId, String role) throws SQLException {
        List<Conversation> result = new ArrayList<>();
        String sql;
        if ("INSTITUTE".equalsIgnoreCase(role)) {
            sql = "SELECT id, institute_id, freelancer_id, created_at FROM Conversations WHERE institute_id = ? ORDER BY created_at DESC";
        } else {
            sql = "SELECT id, institute_id, freelancer_id, created_at FROM Conversations WHERE freelancer_id = ? ORDER BY created_at DESC";
        }

        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Conversation(
                            rs.getInt("id"),
                            rs.getInt("institute_id"),
                            rs.getInt("freelancer_id"),
                            rs.getTimestamp("created_at")));
                }
            }
        }
        return result;
    }

    // Get message history
    public static List<Message> getMessages(int conversationId) {
        List<Message> result = new ArrayList<>();
        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, sender_id, body, created_at, is_read FROM Messages WHERE conversation_id = ? ORDER BY created_at ASC")) {
            ps.setInt(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getString("body"),
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_read")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Save new message
    public static void addMessage(int conversationId, int senderId, String body) {
        Conversation conv = getConversation(conversationId);
        if (conv == null) {
            throw new IllegalArgumentException("Conversation not found");
        }
        if (senderId != conv.instituteId && senderId != conv.freelancerId) {
            throw new IllegalArgumentException("User not part of conversation");
        }

        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Messages (conversation_id, sender_id, body, is_read) VALUES (?, ?, ?, FALSE)")) {
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, body);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB Error", e);
        }
    }

    // Update read status
    public static void markAsRead(int conversationId, int userId) {
        // Mark all messages in this conversation NOT sent by me as read
        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Messages SET is_read = TRUE WHERE conversation_id = ? AND sender_id != ? AND is_read = FALSE")) {
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Count unread
    public static int getUnreadCount(int userId) {
        // Count unread messages in conversations I am part of, sent by others
        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM Messages m " +
                                "JOIN Conversations c ON m.conversation_id = c.id " +
                                "WHERE m.is_read = FALSE AND m.sender_id != ? " +
                                "AND (c.institute_id = ? OR c.freelancer_id = ?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Init new chat
    public static int createConversation(int instituteId, int freelancerId) {
        // Check if exists
        try (Connection conn = DB.getConnection()) {
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT id FROM Conversations WHERE institute_id = ? AND freelancer_id = ?")) {
                ps.setInt(1, instituteId);
                ps.setInt(2, freelancerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return rs.getInt("id");
                }
            }

            // Create
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Conversations (institute_id, freelancer_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, instituteId);
                ps.setInt(2, freelancerId);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB Error", e);
        }
        return -1;
    }

    public static Conversation getConversation(int id) {
        try (Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, institute_id, freelancer_id, created_at FROM Conversations WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Conversation(
                            rs.getInt("id"),
                            rs.getInt("institute_id"),
                            rs.getInt("freelancer_id"),
                            rs.getTimestamp("created_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}