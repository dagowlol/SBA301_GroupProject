import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import 'bootstrap/dist/css/bootstrap.min.css'
import './index.css'
import App from './App'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // Disable retries for auth/permission errors so UI fails fast!
        if (error?.code === 1001 || error?.code === 1002) return false;
        // Otherwise retry up to 3 times
        return failureCount < 3;
      },
      refetchOnWindowFocus: false, // Don't auto-fetch when switching tabs to save bandwidth
    },
  },
})

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </StrictMode>,
)
