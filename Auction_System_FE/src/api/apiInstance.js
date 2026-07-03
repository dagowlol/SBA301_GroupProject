const BASE_URL = 'http://localhost:8080/api/v1';

// Helper to get cookie by name
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return '';
}

/**
 * Custom wrapper for standard fetch to handle JSON requests, error boundaries,
 * and mapping properties from/to the backend.
 */
export async function apiRequest(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const storedToken = sessionStorage.getItem('accessToken');
  const csrfToken = getCookie('csrf_token');

  const headers = {
    ...(storedToken ? { 'Authorization': `Bearer ${storedToken}` } : {}),
    ...(csrfToken ? { 'X-CSRF-TOKEN': csrfToken } : {}),
    ...options.headers,
  };

  // Only set application/json if not using FormData and not explicitly overridden
  if (!(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  
  // If headers['Content-Type'] is explicitly set to null/undefined, delete it
  if (!headers['Content-Type']) {
      delete headers['Content-Type'];
  }

  const config = {
    credentials: 'include', // Ensure cookies (refresh_token, csrf_token) are sent
    ...options,
    headers,
  };

  try {
    let response = await fetch(url, config);

    // If 401 Unauthorized and not an auth endpoint, attempt automatic refresh
    if (response.status === 401 && !path.includes('/auth/')) {
      try {
        const refreshUrl = `${BASE_URL}/auth/refresh`;
        const currentCsrf = getCookie('csrf_token');
        const refreshResponse = await fetch(refreshUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(currentCsrf ? { 'X-CSRF-TOKEN': currentCsrf } : {}),
          },
          credentials: 'include',
        });

        if (refreshResponse.ok) {
          const refreshData = await refreshResponse.json();
          const newAccessToken = refreshData.result?.accessToken;
          if (newAccessToken) {
            sessionStorage.setItem('accessToken', newAccessToken);
            // Retry original request with new token
            config.headers['Authorization'] = `Bearer ${newAccessToken}`;
            response = await fetch(url, config);
          }
        } else {
          // Refresh failed (refresh token expired or invalid), clean up
          sessionStorage.removeItem('accessToken');
          window.location.href = '/';
        }
      } catch (refreshErr) {
        console.error('Auto refresh token failed:', refreshErr);
        sessionStorage.removeItem('accessToken');
        window.location.href = '/';
      }
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      const error = new Error(errorData.message || `HTTP error! status: ${response.status}`);
      error.code = errorData.code;
      throw error;
    }

    // For DELETE or empty responses
    if (response.status === 204 || path.includes('delete') || options.method === 'DELETE') {
      const data = await response.json().catch(() => ({}));
      return data;
    }

    const data = await response.json();
    return data.result; // Standard Spring Boot ApiResponse wrapping
  } catch (error) {
    console.error(`API Request failed for ${url}:`, error);
    // If fetch failed due to CORS or network error, and we have a stored token, it might be an unhandled 401 CORS block
    if (error.message?.includes('Failed to fetch') && sessionStorage.getItem('accessToken') && !path.includes('/auth/')) {
      console.warn('Network/CORS error detected with active token. Attempting cleanup...');
      sessionStorage.removeItem('accessToken');
      window.location.href = '/';
    }
    throw error;
  }
}

