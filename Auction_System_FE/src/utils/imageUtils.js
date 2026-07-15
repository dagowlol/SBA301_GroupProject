/**
 * Image URL utility � resolves backend-relative image paths to full URLs.
 *
 * The backend stores uploaded images under the /uploads directory and returns
 * paths like "/uploads/filename.jpg". The frontend must prefix these with the
 * backend origin so the browser can fetch them.
 */

const BACKEND_ORIGIN = 'http://localhost:8080';

/**
 * Resolves an image URL:
 *  - If already a full URL (http/https) -> returned as-is.
 *  - If a relative backend path (starts with "/") -> prefixed with backend origin.
 *  - Otherwise -> returns null so the caller can fall back to a placeholder.
 *
 * @param {string|null|undefined} url
 * @returns {string|null}
 */
export function resolveImageUrl(url) {
  if (!url) return null;
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/')) return `${BACKEND_ORIGIN}${url}`;
  return url;
}
