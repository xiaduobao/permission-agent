import { useEffect, useRef, useState } from 'react';
import { Button, Card, Input, List, Space, Tag, message, Typography, Alert } from 'antd';
import { SendOutlined, RobotOutlined, UserOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import { aiApi, streamAiChat } from '../../api';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

interface PendingConfirm {
  actionId: number;
  tool: string;
  preview: string;
}

interface SessionMeta {
  sessionId: string;
  title: string;
  updateTime: string;
}

const SESSION_KEY = 'pa_ai_session_id';

const quickQuestions = [
  '查询我当前的所有权限',
  '查询 zhangsan 的权限',
  '列出待处理风险',
  '推荐研发工程师权限配置',
];

export default function AiChat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string | null>(
    () => localStorage.getItem(SESSION_KEY)
  );
  const [sessions, setSessions] = useState<SessionMeta[]>([]);
  const [toolStatus, setToolStatus] = useState<string | null>(null);
  const [pendingConfirm, setPendingConfirm] = useState<PendingConfirm | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  const loadSessions = async () => {
    try {
      const res = await aiApi.listSessions();
      setSessions(res.data || []);
    } catch { /* ignore */ }
  };

  const loadHistory = async (sid: string) => {
    try {
      const res = await aiApi.getSession(sid);
      const history = (res.data || []).map((m: any) => ({
        role: m.role as 'user' | 'assistant',
        content: m.content || '',
      }));
      setMessages(history);
    } catch {
      setMessages([]);
    }
  };

  useEffect(() => {
    loadSessions();
    if (sessionId) {
      loadHistory(sessionId);
    }
  }, []);

  const switchSession = (sid: string) => {
    setSessionId(sid);
    localStorage.setItem(SESSION_KEY, sid);
    setPendingConfirm(null);
    loadHistory(sid);
  };

  const newSession = () => {
    setSessionId(null);
    localStorage.removeItem(SESSION_KEY);
    setMessages([]);
    setPendingConfirm(null);
  };

  const deleteSession = async (sid: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await aiApi.deleteSession(sid);
      if (sessionId === sid) newSession();
      loadSessions();
    } catch {
      message.error('删除失败');
    }
  };

  const send = async (text?: string) => {
    const msg = text || input.trim();
    if (!msg || loading) return;

    setInput('');
    setPendingConfirm(null);
    setToolStatus(null);
    setMessages((prev) => [...prev, { role: 'user', content: msg }]);
    setLoading(true);

    let assistantContent = '';
    setMessages((prev) => [...prev, { role: 'assistant', content: '' }]);

    try {
      await streamAiChat(
        msg,
        sessionId,
        (chunk) => {
          assistantContent += chunk;
          setMessages((prev) => {
            const copy = [...prev];
            copy[copy.length - 1] = { role: 'assistant', content: assistantContent };
            return copy;
          });
          bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
        },
        (sid) => {
          if (sid) {
            setSessionId(sid);
            localStorage.setItem(SESSION_KEY, sid);
            loadSessions();
          }
        },
        (tool) => setToolStatus(`正在执行: ${tool}...`),
        (data) => {
          setToolStatus(null);
          setPendingConfirm(data);
        }
      );
    } catch (e: any) {
      message.error(e.message || '对话失败');
    } finally {
      setLoading(false);
      setToolStatus(null);
    }
  };

  const handleConfirm = async (confirm: boolean) => {
    if (!pendingConfirm) return;
    try {
      if (confirm) {
        const res = await aiApi.confirmAction(pendingConfirm.actionId);
        message.success('操作已执行');
        setMessages((prev) => [...prev, { role: 'assistant', content: res.data || '操作已完成' }]);
      } else {
        await aiApi.rejectAction(pendingConfirm.actionId);
        message.info('已取消操作');
        setMessages((prev) => [...prev, { role: 'assistant', content: '操作已取消' }]);
      }
      setPendingConfirm(null);
      if (sessionId) loadHistory(sessionId);
    } catch (e: any) {
      message.error(e.message || '操作失败');
    }
  };

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 180px)', gap: 16 }}>
      <Card
        title="会话"
        size="small"
        style={{ width: 220, flexShrink: 0 }}
        extra={<Button type="text" icon={<PlusOutlined />} onClick={newSession} />}
        bodyStyle={{ padding: 0, maxHeight: 'calc(100vh - 260px)', overflow: 'auto' }}
      >
        <List
          size="small"
          dataSource={sessions}
          locale={{ emptyText: '暂无历史会话' }}
          renderItem={(item) => (
            <List.Item
              style={{
                cursor: 'pointer',
                background: sessionId === item.sessionId ? '#e6f4ff' : undefined,
                padding: '8px 12px',
              }}
              onClick={() => switchSession(item.sessionId)}
            >
              <div style={{ flex: 1, overflow: 'hidden' }}>
                <Typography.Text ellipsis style={{ fontSize: 13 }}>
                  {item.title || '新对话'}
                </Typography.Text>
              </div>
              <Button
                type="text"
                size="small"
                icon={<DeleteOutlined />}
                onClick={(e) => deleteSession(item.sessionId, e)}
              />
            </List.Item>
          )}
        />
      </Card>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <h2>AI权限助手</h2>
        <Space wrap style={{ marginBottom: 16 }}>
          {quickQuestions.map((q) => (
            <Tag key={q} color="blue" style={{ cursor: 'pointer' }} onClick={() => send(q)}>{q}</Tag>
          ))}
        </Space>

        {toolStatus && (
          <Alert message={toolStatus} type="info" showIcon style={{ marginBottom: 8 }} />
        )}

        {pendingConfirm && (
          <Alert
            type="warning"
            showIcon
            style={{ marginBottom: 8 }}
            message="待确认操作"
            description={
              <div>
                <p>{pendingConfirm.preview}</p>
                <Space>
                  <Button type="primary" size="small" onClick={() => handleConfirm(true)}>确认执行</Button>
                  <Button size="small" onClick={() => handleConfirm(false)}>取消</Button>
                </Space>
              </div>
            }
          />
        )}

        <Card style={{ flex: 1, overflow: 'auto', marginBottom: 16 }} styles={{ body: { height: '100%' } }}>
          {messages.length === 0 && (
            <div style={{ textAlign: 'center', color: '#999', marginTop: 80 }}>
              <RobotOutlined style={{ fontSize: 48 }} />
              <p>您好，我是企业权限管理Agent，可以帮您查询权限、分析风险、推荐配置</p>
            </div>
          )}
          {messages.map((m, i) => (
            <div key={i} style={{ display: 'flex', marginBottom: 16, justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start' }}>
              {m.role === 'assistant' && <RobotOutlined style={{ marginRight: 8, marginTop: 4 }} />}
              <div style={{
                maxWidth: '70%', padding: '8px 16px', borderRadius: 8,
                background: m.role === 'user' ? '#667eea' : '#f5f5f5',
                color: m.role === 'user' ? '#fff' : '#333',
              }}>
                {m.role === 'assistant' ? <ReactMarkdown>{m.content}</ReactMarkdown> : m.content}
              </div>
              {m.role === 'user' && <UserOutlined style={{ marginLeft: 8, marginTop: 4 }} />}
            </div>
          ))}
          <div ref={bottomRef} />
        </Card>

        <Space.Compact style={{ width: '100%' }}>
          <Input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onPressEnter={() => send()}
            placeholder="输入权限相关问题，如：查询张三的所有权限"
            disabled={loading}
            size="large"
          />
          <Button type="primary" icon={<SendOutlined />} size="large" loading={loading} onClick={() => send()}>
            发送
          </Button>
        </Space.Compact>
      </div>
    </div>
  );
}
