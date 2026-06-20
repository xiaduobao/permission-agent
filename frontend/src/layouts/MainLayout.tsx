import {
  ApartmentOutlined,
  ApiOutlined,
  AuditOutlined,
  BankOutlined,
  DashboardOutlined,
  FileTextOutlined,
  HistoryOutlined,
  MenuOutlined,
  MessageOutlined,
  RobotOutlined,
  SettingOutlined,
  TeamOutlined,
  UserOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import { Layout, Menu, Dropdown, Avatar, theme } from 'antd';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore, MenuItem } from '../store/authStore';

const { Header, Sider, Content } = Layout;

const iconMap: Record<string, React.ReactNode> = {
  SettingOutlined: <SettingOutlined />,
  UserOutlined: <UserOutlined />,
  TeamOutlined: <TeamOutlined />,
  MenuOutlined: <MenuOutlined />,
  ApartmentOutlined: <ApartmentOutlined />,
  ApiOutlined: <ApiOutlined />,
  BankOutlined: <BankOutlined />,
  RobotOutlined: <RobotOutlined />,
  MessageOutlined: <MessageOutlined />,
  WarningOutlined: <WarningOutlined />,
  FileTextOutlined: <FileTextOutlined />,
  AuditOutlined: <AuditOutlined />,
  HistoryOutlined: <HistoryOutlined />,
  DashboardOutlined: <DashboardOutlined />,
};

function toMenuItems(menus: MenuItem[]): any[] {
  return menus
    .filter((m) => m.type !== 3)
    .map((m) => ({
      key: m.path || String(m.id),
      icon: m.icon ? iconMap[m.icon] : undefined,
      label: m.name,
      children: m.children?.length ? toMenuItems(m.children) : undefined,
    }));
}

export default function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { token: themeToken } = theme.useToken();
  const { menus, nickname, username, logout } = useAuthStore();

  const menuItems = toMenuItems(menus.length ? menus : getDefaultMenus());

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" width={220}>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 600, fontSize: 16 }}>
          AI权限Agent
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
          <Dropdown
            menu={{
              items: [{ key: 'logout', label: '退出登录', onClick: () => { logout(); navigate('/login'); } }],
            }}
          >
            <span style={{ cursor: 'pointer' }}>
              <Avatar style={{ background: themeToken.colorPrimary, marginRight: 8 }}>{nickname?.[0] || 'U'}</Avatar>
              {nickname || username}
            </span>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8, minHeight: 360 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}

function getDefaultMenus(): MenuItem[] {
  return [
    { id: 14, name: '工作台', path: '/dashboard', type: 2, icon: 'DashboardOutlined' },
    {
      id: 1, name: '系统管理', path: '/system', type: 1, icon: 'SettingOutlined',
      children: [
        { id: 2, name: '用户管理', path: '/system/users', type: 2, icon: 'UserOutlined' },
        { id: 3, name: '角色管理', path: '/system/roles', type: 2, icon: 'TeamOutlined' },
        { id: 4, name: '菜单管理', path: '/system/menus', type: 2, icon: 'MenuOutlined' },
        { id: 5, name: '部门管理', path: '/system/depts', type: 2, icon: 'ApartmentOutlined' },
        { id: 6, name: '接口权限', path: '/system/apis', type: 2, icon: 'ApiOutlined' },
        { id: 7, name: '租户管理', path: '/system/tenants', type: 2, icon: 'BankOutlined' },
      ],
    },
    {
      id: 8, name: 'AI智能运维', path: '/ai', type: 1, icon: 'RobotOutlined',
      children: [
        { id: 9, name: 'AI权限助手', path: '/ai/chat', type: 2, icon: 'MessageOutlined' },
        { id: 10, name: '权限风险', path: '/ai/risks', type: 2, icon: 'WarningOutlined' },
        { id: 11, name: '权限模板', path: '/ai/templates', type: 2, icon: 'FileTextOutlined' },
      ],
    },
    {
      id: 12, name: '审计日志', path: '/audit', type: 1, icon: 'AuditOutlined',
      children: [
        { id: 13, name: '操作日志', path: '/audit/logs', type: 2, icon: 'HistoryOutlined' },
      ],
    },
  ];
}
