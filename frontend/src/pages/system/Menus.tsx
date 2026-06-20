import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Select, Space, Table, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { menuApi } from '../../api';

export default function Menus() {
  const [data, setData] = useState<any[]>([]);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    const res = await menuApi.tree();
    setData(res.data);
  };

  useEffect(() => { load(); }, []);

  const handleSave = async () => {
    const values = await form.validateFields();
    await menuApi.save(values);
    message.success('保存成功');
    setOpen(false);
    form.resetFields();
    load();
  };

  const columns = [
    { title: '菜单名', dataIndex: 'name' },
    { title: '路径', dataIndex: 'path' },
    { title: '权限标识', dataIndex: 'permission' },
    { title: '类型', dataIndex: 'type', render: (t: number) => ['', '目录', '菜单', '按钮'][t] },
    {
      title: '操作', render: (_: any, record: any) => (
        <Popconfirm title="确认删除?" onConfirm={async () => { await menuApi.delete(record.id); load(); }}>
          <Button type="link" danger>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>新增菜单</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={false} />
      <Modal title="菜单" open={open} onOk={handleSave} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="path" label="路径"><Input /></Form.Item>
          <Form.Item name="permission" label="权限标识"><Input /></Form.Item>
          <Form.Item name="type" label="类型" initialValue={2}>
            <Select options={[{ label: '目录', value: 1 }, { label: '菜单', value: 2 }, { label: '按钮', value: 3 }]} />
          </Form.Item>
          <Form.Item name="icon" label="图标"><Input /></Form.Item>
          <Form.Item name="parentId" label="父级ID" initialValue={0}><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
