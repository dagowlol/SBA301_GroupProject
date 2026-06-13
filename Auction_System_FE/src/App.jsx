import { AppContextProvider } from './context/AppContext';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './router/AppRoutes';

export default function App() {
  return (
    <AuthProvider>
      <AppContextProvider>
        <AppRoutes />
      </AppContextProvider>
    </AuthProvider>
  );
}
