import { useEffect, useState } from 'react';
import { Table } from 'antd';
import { dashboardApi } from '../../api';

export default function Logs() {
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);

  useEffect(() => {
    dashboardApi.logs({ pageNum: page, pageSize: 10 }).then((res) => {
      setData(res.data.records);
      setTotal(res.data.total);
    });
  }, [page]);

  const columns = [
    { title: '操作人', dataIndex: 'username', width: 100 },
    { title: '模块', dataIndex: 'module', width: 100 },
    { title: '操作', dataIndex: 'action', width: 100 },
    { title: '详情', dataIndex: 'detail', ellipsis: true },
    { title: '时间', dataIndex: 'createTime', width: 180 },
  ];

  return (
    <div>
      <h2>操作审计日志</h2>
      <Table rowKey="id" columns={columns} dataSource={data} pagination={{ current: page, total, onChange: setPage }} />
    </div>
  );
}
