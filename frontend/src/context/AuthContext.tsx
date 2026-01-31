import { createContext, useState, useContext, ReactNode } from 'react';

interface AuthContextType {
  memberId: string | null;
  login: (id: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [memberId, setMemberId] = useState<string | null>(localStorage.getItem('memberId'));

  const login = (id: string) => {
    setMemberId(id);
    localStorage.setItem('memberId', id);
  };

  const logout = () => {
    setMemberId(null);
    localStorage.removeItem('memberId');
  };

  return (
    <AuthContext.Provider value={{ memberId, login, logout }}>
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
