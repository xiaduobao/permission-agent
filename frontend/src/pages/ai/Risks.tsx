import { useEffect, useState } from 'react';
import { Button, Space, Table, Tag, message } from 'antd';
import { SyncOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import { dashboardApi } from '../../api';

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待处理', color: 'red' },
  1: { text: '已处理', color: 'green' },
  2: { text: '已忽略', color: 'default' },
};

export default function Risks() {
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);

  const load = async () => {
    const res = await dashboardApi.risks({ pageNum: page, pageSize: 10 });
    setData(res.data.records);
    setTotal(res.data.total);
  };

  useEffect(() => { load(); }, [page]);

  const handleCheck = async () => {
    setLoading(true);
    try {
      await dashboardApi.triggerCheck();
      message.success('巡检完成');
      load();
    } finally {
      setLoading(false);
    }
  };

  const columns = [
    { title: '用户', dataIndex: 'username' },
    { title: '风险类型', dataIndex: 'riskType', render: (t: string) => <Tag color="orange">{t}</Tag> },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '建议', dataIndex: 'suggestion', ellipsis: true },
    { title: '状态', dataIndex: 'status', render: (s: number) => {
      const m = statusMap[s] || statusMap[0];
      return <Tag color={m.color}>{m.text}</Tag>;
    }},
    {
      title: '操作', render: (_: any, record: any) => record.status === 0 ? (
        <Space>
          <Button type="link" icon={<CheckOutlined />} onClick={async () => { await dashboardApi.handleRisk(record.id, 1); load(); }}>处理</Button>
          <Button type="link" icon={<CloseOutlined />} onClick={async () => { await dashboardApi.handleRisk(record.id, 2); load(); }}>忽略</Button>
        </Space>
      ) : null,
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <h2 style={{ margin: 0 }}>权限风险治理</h2>
        <Button type="primary" icon={<SyncOutlined />} loading={loading} onClick={handleCheck}>立即巡检</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={{ current: page, total, onChange: setPage }} />
    </div>
  );
}
