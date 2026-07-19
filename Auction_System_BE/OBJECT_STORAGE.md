# Object storage

Images are uploaded directly from the browser to S3-compatible storage with a five-minute presigned PUT URL. The database stores only the object key. Existing `/uploads/...` values remain readable for backward compatibility.

## Local development with MinIO

1. Install Docker Desktop.
2. Start storage: `docker compose -f docker-compose.storage.yml up -d`
3. Start the backend and frontend normally.
4. MinIO console: `http://localhost:9001` (`minioadmin` / `minioadmin`).

The compose file creates the `auction-images` bucket and persists objects in the `minio-data` volume.

## Amazon S3 production configuration

Set these environment variables:

```text
STORAGE_BUCKET=your-private-bucket
STORAGE_REGION=ap-southeast-1
STORAGE_ENDPOINT=
STORAGE_ACCESS_KEY=...
STORAGE_SECRET_KEY=...
STORAGE_USE_DEFAULT_CREDENTIALS=false
STORAGE_PUBLIC_BASE_URL=https://your-cloudfront-domain
```

Prefer an IAM role instead of long-lived keys when the application runs on AWS; set `STORAGE_USE_DEFAULT_CREDENTIALS=true` and omit the access/secret key values. Keep the bucket private when `STORAGE_PUBLIC_BASE_URL` is empty; the backend then returns short-lived presigned GET URLs.

Configure an S3 lifecycle rule to remove uncommitted uploads under `items/` after an appropriate retention window. Replaced and deleted item images are removed automatically after the database transaction commits.
