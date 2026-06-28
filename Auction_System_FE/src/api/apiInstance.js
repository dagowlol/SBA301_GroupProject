const BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Custom wrapper for standard fetch to handle JSON requests, error boundaries,
 * and mapping properties from/to the backend.
 */
export async function apiRequest(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const storedToken = sessionStorage.getItem('accessToken');
  const headers = {
    ...(storedToken ? { 'Authorization': `Bearer ${storedToken}` } : {}),
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
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
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
    throw error;
  }
}

