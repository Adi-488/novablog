import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getAccessToken, getStoredUser } from '../api/client';

/**
 * React hook for WebSocket-based real-time collaboration on a post.
 *
 * Sprint 4: Handles presence tracking, content broadcasting,
 * cursor sync, and auto-save via STOMP over SockJS.
 *
 * @param {string} postId - The post being edited
 * @param {function} onRemoteEdit - Callback when a remote user edits content
 * @param {function} onRemoteTitleEdit - Callback when a remote user edits the title
 * @returns {{ presence, sendEdit, sendTitleEdit, sendCursor, connected }}
 */
export default function useCollaboration(postId, onRemoteEdit, onRemoteTitleEdit) {
  const clientRef = useRef(null);
  const [presence, setPresence] = useState([]);
  const [connected, setConnected] = useState(false);
  const user = getStoredUser();

  useEffect(() => {
    if (!postId || !user) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: {
        Authorization: `Bearer ${getAccessToken()}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      setConnected(true);

      // Subscribe to post edits
      client.subscribe(`/topic/post/${postId}`, (message) => {
        const msg = JSON.parse(message.body);
        if (msg.userId === user.id) return; // Ignore own messages

        if (msg.type === 'CONTENT_UPDATE' && onRemoteEdit) {
          onRemoteEdit(msg.content, msg.userId, msg.userName);
        } else if (msg.type === 'TITLE_UPDATE' && onRemoteTitleEdit) {
          onRemoteTitleEdit(msg.title, msg.userId, msg.userName);
        }
      });

      // Subscribe to presence updates
      client.subscribe(`/topic/post/${postId}/presence`, (message) => {
        const presenceList = JSON.parse(message.body);
        setPresence(presenceList);
      });

      // Announce join
      client.publish({
        destination: `/app/post/${postId}/join`,
        body: JSON.stringify({
          type: 'JOIN',
          postId,
          userId: user.id,
          userName: user.email?.split('@')[0] || 'User',
          avatarUrl: user.avatarUrl || null,
          timestamp: Date.now(),
        }),
      });
    };

    client.onDisconnect = () => {
      setConnected(false);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      // Announce leave before disconnecting
      if (client.connected) {
        client.publish({
          destination: `/app/post/${postId}/leave`,
          body: JSON.stringify({
            type: 'LEAVE',
            postId,
            userId: user.id,
            userName: user.email?.split('@')[0] || 'User',
            timestamp: Date.now(),
          }),
        });
      }
      client.deactivate();
    };
  }, [postId]);

  const sendEdit = useCallback((content) => {
    if (!clientRef.current?.connected || !user) return;
    clientRef.current.publish({
      destination: `/app/post/${postId}/edit`,
      body: JSON.stringify({
        type: 'CONTENT_UPDATE',
        postId,
        userId: user.id,
        userName: user.email?.split('@')[0] || 'User',
        content,
        timestamp: Date.now(),
      }),
    });
  }, [postId, user]);

  const sendTitleEdit = useCallback((title) => {
    if (!clientRef.current?.connected || !user) return;
    clientRef.current.publish({
      destination: `/app/post/${postId}/edit`,
      body: JSON.stringify({
        type: 'TITLE_UPDATE',
        postId,
        userId: user.id,
        userName: user.email?.split('@')[0] || 'User',
        title,
        timestamp: Date.now(),
      }),
    });
  }, [postId, user]);

  const sendCursor = useCallback((cursorPosition) => {
    if (!clientRef.current?.connected || !user) return;
    clientRef.current.publish({
      destination: `/app/post/${postId}/cursor`,
      body: JSON.stringify({
        type: 'CURSOR',
        postId,
        userId: user.id,
        userName: user.email?.split('@')[0] || 'User',
        cursorPosition,
        timestamp: Date.now(),
      }),
    });
  }, [postId, user]);

  return { presence, sendEdit, sendTitleEdit, sendCursor, connected };
}
