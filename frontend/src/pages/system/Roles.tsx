import { useEffect, useState } from 'react';
import { Button, Form, Input, Modal, Select, Space, Table, Tag, message, Popconfirm } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { roleApi } from '../../api';

const dataScopeOptions = [
  { label: '全部数据', value: 1 },
  { label: '本部门', value: 2 },
  { label: '本部门及下级', value: 3 },
  { label: '仅本人', value: 4 },
  { label: '自定义', value: 5 },
];

export default function Roles() {
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();

  const load = async () => {
    const res = await roleApi.page({ pageNum: page, pageSize: 10 });
    setData(res.data.records);
    setTotal(res.data.total);
  };

  useEffect(() => { load(); }, [page]);

  const handleSave = async () => {
    const values = await form.validateFields();
    await roleApi.save(values);
    message.success('保存成功');
    setOpen(false);
    form.resetFields();
    load();
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '角色名', dataIndex: 'name' },
    { title: '标识', dataIndex: 'code' },
    { title: '数据范围', dataIndex: 'dataScope', render: (v: number) => dataScopeOptions.find((o) => o.value === v)?.label },
    { title: '状态', dataIndex: 'status', render: (s: number) => s === 1 ? <Tag color="green">启用</Tag> : <Tag color="red">禁用</Tag> },
    {
      title: '操作', render: (_: any, record: any) => (
        <Popconfirm title="确认删除?" onConfirm={async () => { await roleApi.delete(record.id); load(); }}>
          <Button type="link" danger>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true); }}>新增角色</Button>
      </Space>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={{ current: page, total, onChange: setPage }} />
      <Modal title="角色" open={open} onOk={handleSave} onCancel={() => setOpen(false)} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="角色名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="code" label="标识" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="dataScope" label="数据范围" initialValue={4}>
            <Select options={dataScopeOptions} />
          </Form.Item>
          <Form.Item name="remark" label="备注"><Input.TextArea /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
