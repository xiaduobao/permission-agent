import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Space, Table, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { deptApi } from '../../api';

export default function Depts() {
  const [data, setData] = useState<any[]>([]);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    const res = await deptApi.tree();
    setData(res.data);
  };

  useEffect(() => { load(); }, []);

  const handleSave = async () => {
    const values = await form.validateFields();
    await deptApi.save(values);
    message.success('保存成功');
    setOpen(false);
    form.resetFields();
    load();
  };

  const columns = [
    { title: '部门名', dataIndex: 'name' },
    { title: '排序', dataIndex: 'sort' },
    {
      title: '操作', render: (_: any, record: any) => (
        <Popconfirm title="确认删除?" onConfirm={async () => { await deptApi.delete(record.id); load(); }}>
          <Button type="link" danger>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>新增部门</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={false} />
      <Modal title="部门" open={open} onOk={handleSave} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="parentId" label="父级ID" initialValue={0}><Input /></Form.Item>
          <Form.Item name="sort" label="排序" initialValue={0}><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
