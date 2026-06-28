const BASE_URL = 'http://localhost:8080/api/v1';

/**
 * Custom wrapper for standard fetch to handle JSON requests, error boundaries,
 * and mapping properties from/to the backend.
 */
export async function apiRequest(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const storedToken = sessionStorage.getItem('accessToken');
  const headers = {
    'Content-Type': 'application/json',
    ...(storedToken ? { 'Authorization': `Bearer ${storedToken}` } : {}),
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);
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
    throw error;
  }
}

