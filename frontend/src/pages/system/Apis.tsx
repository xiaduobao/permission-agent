import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Select, Space, Table, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { apiPermApi } from '../../api';

export default function Apis() {
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    const res = await apiPermApi.page({ pageNum: page, pageSize: 10 });
    setData(res.data.records);
    setTotal(res.data.total);
  };

  useEffect(() => { load(); }, [page]);

  const handleSave = async () => {
    const values = await form.validateFields();
    await apiPermApi.save(values);
    message.success('保存成功');
    setOpen(false);
    form.resetFields();
    load();
  };

  const columns = [
    { title: '名称', dataIndex: 'name' },
    { title: '路径', dataIndex: 'path' },
    { title: '方法', dataIndex: 'method' },
    { title: '权限标识', dataIndex: 'permission' },
    {
      title: '操作', render: (_: any, record: any) => (
        <Popconfirm title="确认删除?" onConfirm={async () => { await apiPermApi.delete(record.id); load(); }}>
          <Button type="link" danger>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>新增接口</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={{ current: page, total, onChange: setPage }} />
      <Modal title="接口权限" open={open} onOk={handleSave} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="path" label="路径" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="method" label="方法" initialValue="GET">
            <Select options={['GET', 'POST', 'PUT', 'DELETE'].map((m) => ({ label: m, value: m }))} />
          </Form.Item>
          <Form.Item name="permission" label="权限标识"><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
