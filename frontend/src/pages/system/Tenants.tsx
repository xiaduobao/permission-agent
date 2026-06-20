import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Space, Table, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { tenantApi } from '../../api';

export default function Tenants() {
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    const res = await tenantApi.page({ pageNum: page, pageSize: 10 });
    setData(res.data.records);
    setTotal(res.data.total);
  };

  useEffect(() => { load(); }, [page]);

  const handleSave = async () => {
    const values = await form.validateFields();
    await tenantApi.save(values);
    message.success('保存成功');
    setOpen(false);
    form.resetFields();
    load();
  };

  const columns = [
    { title: '租户名', dataIndex: 'name' },
    { title: '编码', dataIndex: 'code' },
    { title: '状态', dataIndex: 'status', render: (s: number) => s === 1 ? <Tag color="green">启用</Tag> : <Tag color="red">禁用</Tag> },
    {
      title: '操作', render: (_: any, record: any) => (
        <Popconfirm title="确认删除?" onConfirm={async () => { await tenantApi.delete(record.id); load(); }}>
          <Button type="link" danger>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>新增租户</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={{ current: page, total, onChange: setPage }} />
      <Modal title="租户" open={open} onOk={handleSave} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="code" label="编码" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
