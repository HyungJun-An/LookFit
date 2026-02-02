import { createContext, useState, useContext, type ReactNode } from 'react';

interface AuthContextType {
  memberId: string | null;
  token: string | null;
  login: (id: string, authToken: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [memberId, setMemberId] = useState<string | null>(localStorage.getItem('memberId'));
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const login = (id: string, authToken: string) => {
    setMemberId(id);
    setToken(authToken);
    localStorage.setItem('memberId', id);
    localStorage.setItem('token', authToken);
  };

  const logout = () => {
    setMemberId(null);
    setToken(null);
    localStorage.removeItem('memberId');
    localStorage.removeItem('token');
    localStorage.removeItem('memberName');
  };

  const isAuthenticated = !!(memberId && token);

  return (
    <AuthContext.Provider value={{ memberId, token, login, logout, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
