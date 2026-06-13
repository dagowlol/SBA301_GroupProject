import { AppContextProvider } from './context/AppContext';
import AppRoutes from './router/AppRoutes';

export default function App() {
  return (
    <AppContextProvider>
      <AppRoutes />
    </AppContextProvider>
  );
}
