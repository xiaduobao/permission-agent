import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Select, Space, Table, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { userApi, roleApi, deptApi } from '../../api';

export default function Users() {
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [open, setOpen] = useState(false);
  const [roles, setRoles] = useState<any[]>([]);
  const [depts, setDepts] = useState<any[]>([]);
  const [form] = Form.useForm();

  const load = async () => {
    const res = await userApi.page({ pageNum: page, pageSize: 10, keyword });
    setData(res.data.records);
    setTotal(res.data.total);
  };

  useEffect(() => { load(); }, [page, keyword]);
  useEffect(() => {
    roleApi.listAll().then((r) => setRoles(r.data));
    deptApi.tree().then((r) => setDepts(flattenDepts(r.data)));
  }, []);

  const flattenDepts = (tree: any[], result: any[] = []): any[] => {
    tree.forEach((d) => { result.push(d); if (d.children) flattenDepts(d.children, result); });
    return result;
  };

  const handleSave = async () => {
    const values = await form.validateFields();
    await userApi.save(values);
    message.success('保存成功');
    setOpen(false);
    form.resetFields();
    load();
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '用户名', dataIndex: 'username' },
    { title: '昵称', dataIndex: 'nickname' },
    { title: '岗位', dataIndex: 'position' },
    { title: '状态', dataIndex: 'status', render: (s: number) => s === 1 ? <Tag color="green">启用</Tag> : <Tag color="red">禁用</Tag> },
    {
      title: '操作', render: (_: any, record: any) => (
        <Popconfirm title="确认删除?" onConfirm={async () => { await userApi.delete(record.id); load(); }}>
          <Button type="link" danger>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Input.Search placeholder="搜索用户" onSearch={setKeyword} allowClear />
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>新增用户</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={{ current: page, total, onChange: setPage }} />
      <Modal title="用户" open={open} onOk={handleSave} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="id" hidden><Input /></Form.Item>
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="password" label="密码"><Input.Password placeholder="新增时必填" /></Form.Item>
          <Form.Item name="nickname" label="昵称"><Input /></Form.Item>
          <Form.Item name="position" label="岗位"><Input /></Form.Item>
          <Form.Item name="deptId" label="部门">
            <Select options={depts.map((d) => ({ label: d.name, value: d.id }))} />
          </Form.Item>
          <Form.Item name="roleIds" label="角色">
            <Select mode="multiple" options={roles.map((r) => ({ label: r.name, value: r.id }))} />
          </Form.Item>
          <Form.Item name="status" label="状态" initialValue={1}>
            <Select options={[{ label: '启用', value: 1 }, { label: '禁用', value: 0 }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
