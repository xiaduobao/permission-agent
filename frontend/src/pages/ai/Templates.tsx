import { useEffect, useState } from 'react';
import { Card, Col, Row, Tag } from 'antd';
import { dashboardApi } from '../../api';

export default function Templates() {
  const [templates, setTemplates] = useState<any[]>([]);

  useEffect(() => {
    dashboardApi.templates().then((res) => setTemplates(res.data));
  }, []);

  return (
    <div>
      <h2>AI权限模板</h2>
      <p style={{ color: '#666', marginBottom: 24 }}>基于岗位/场景的智能权限配置模板，可用于一键推荐权限分配</p>
      <Row gutter={[16, 16]}>
        {templates.map((t) => (
          <Col span={8} key={t.id}>
            <Card title={t.name} extra={<Tag color="blue">{t.position}</Tag>}>
              <p><strong>场景：</strong>{t.scene}</p>
              <p><strong>绑定角色：</strong>{t.roleIds}</p>
              <p style={{ color: '#666' }}>{t.description}</p>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
