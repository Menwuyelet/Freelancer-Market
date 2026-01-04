let currentConversationId = null;
let ws = null;

// Get chat list
async function loadConversations() {
    const res = await fetch('api/chat/conversations');
    if (!res.ok) {
        return;
    }
    const data = await res.json();
    const container = document.getElementById('conversations');
    container.innerHTML = '';
    data.forEach(conv => {
        const div = document.createElement('div');
        div.className = 'conversation-item';
        div.textContent = conv.otherName + ' (' + conv.otherRole + ')';
        div.onclick = () => openConversation(conv.id, conv.otherName);
        container.appendChild(div);
    });
}

// Open a specific chat
async function openConversation(id, name) {
    currentConversationId = id;
    document.getElementById('chatHeader').textContent = 'Chat with ' + name;
    await loadMessages();
}

// Load history
async function loadMessages() {
    if (!currentConversationId) return;
    const res = await fetch('api/chat/messages?conversationId=' + currentConversationId);
    if (!res.ok) return;
    const data = await res.json();
    const container = document.getElementById('messages');
    container.innerHTML = '';
    data.forEach(m => {
        const div = document.createElement('div');
        div.className = 'message ' + (m.me ? 'me' : 'them');
        div.textContent = m.body;
        container.appendChild(div);
    });
    container.scrollTop = container.scrollHeight;
}

document.getElementById('chatForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!currentConversationId) return;
    const body = document.getElementById('messageInput').value.trim();
    if (!body) return;
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ conversationId: currentConversationId, body }));
    }
    document.getElementById('messageInput').value = '';
});

// WebSocket connection
function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
    const host = window.location.host;

    const path = window.location.pathname;
    const contextPath = path.substring(0, path.indexOf('/', 1));
    const wsUrl = protocol + host + contextPath + '/ws/chat';
    console.log('Connecting to WebSocket at:', wsUrl);

    ws = new WebSocket(wsUrl);

    ws.onmessage = async (event) => {
        const msg = JSON.parse(event.data);
        if (msg.type === 'message') {
            const isCurrent = msg.conversationId === currentConversationId;
            if (isCurrent) {
                await loadMessages();
            }
        }
    };

    ws.onclose = () => {
        setTimeout(connectWebSocket, 3000);
    };
}

function appendMessage(m) {
    const container = document.getElementById('messages');
    const div = document.createElement('div');
    div.className = 'message ' + (m.me ? 'me' : 'them');
    div.textContent = m.body;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

window.addEventListener('load', () => {
    loadConversations();
    connectWebSocket();
});
