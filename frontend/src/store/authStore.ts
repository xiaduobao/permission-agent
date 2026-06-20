import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface MenuItem {
  id: number;
  name: string;
  path?: string;
  icon?: string;
  type: number;
  children?: MenuItem[];
}

interface AuthState {
  token: string | null;
  username: string | null;
  nickname: string | null;
  menus: MenuItem[];
  permissions: string[];
  setAuth: (data: {
    token: string;
    username: string;
    nickname: string;
    menus: MenuItem[];
    permissions: string[];
  }) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      username: null,
      nickname: null,
      menus: [],
      permissions: [],
      setAuth: (data) => set(data),
      logout: () =>
        set({ token: null, username: null, nickname: null, menus: [], permissions: [] }),
    }),
    { name: 'permission-agent-auth' }
  )
);
