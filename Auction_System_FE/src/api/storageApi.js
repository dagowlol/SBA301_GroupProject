import { apiRequest } from './apiInstance';

export const storageApi = {
  presignImage: (file) => apiRequest('/storage/images/presign', {
    method: 'POST',
    body: JSON.stringify({
      fileName: file.name,
      contentType: file.type,
      fileSize: file.size,
    }),
  }),
};

export async function uploadImageDirect(file) {
  const presigned = await storageApi.presignImage(file);
  const response = await fetch(presigned.uploadUrl, {
    method: 'PUT',
    headers: { 'Content-Type': file.type },
    body: file,
  });
  if (!response.ok) throw new Error('Failed to upload image to object storage.');
  return presigned.objectKey;
}
