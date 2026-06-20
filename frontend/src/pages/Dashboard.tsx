import { useEffect, useState } from 'react';
import { Card, Col, Row, Statistic, Button } from 'antd';
import { UserOutlined, TeamOutlined, WarningOutlined, AlertOutlined } from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import { dashboardApi } from '../api';

export default function Dashboard() {
  const [stats, setStats] = useState<Record<string, number>>({});

  useEffect(() => {
    dashboardApi.stats().then((res) => setStats(res.data));
  }, []);

  const riskChartOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: stats.pendingRisks || 0, name: '待处理风险' },
        { value: (stats.totalRisks || 0) - (stats.pendingRisks || 0), name: '已处理风险' },
      ],
    }],
  };

  const permChartOption = {
    tooltip: {},
    xAxis: { type: 'category', data: ['用户', '角色', '风险'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [stats.userCount || 0, stats.roleCount || 0, stats.totalRisks || 0],
      itemStyle: { color: '#667eea' },
    }],
  };

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>权限管理工作台</h2>
      <Row gutter={[16, 16]}>
        <Col span={6}>
          <Card><Statistic title="用户总数" value={stats.userCount || 0} prefix={<UserOutlined />} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="角色总数" value={stats.roleCount || 0} prefix={<TeamOutlined />} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="待处理风险" value={stats.pendingRisks || 0} prefix={<WarningOutlined />} valueStyle={{ color: '#cf1322' }} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="风险总数" value={stats.totalRisks || 0} prefix={<AlertOutlined />} /></Card>
        </Col>
      </Row>
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col span={12}>
          <Card title="权限风险分布">
            <ReactECharts option={riskChartOption} style={{ height: 300 }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="权限概览">
            <ReactECharts option={permChartOption} style={{ height: 300 }} />
          </Card>
        </Col>
      </Row>
      <Card style={{ marginTop: 24 }} title="快捷操作">
        <Button type="primary" onClick={() => dashboardApi.triggerCheck().then(() => window.location.href = '/ai/risks')}>
          立即执行权限风险巡检
        </Button>
      </Card>
    </div>
  );
}
