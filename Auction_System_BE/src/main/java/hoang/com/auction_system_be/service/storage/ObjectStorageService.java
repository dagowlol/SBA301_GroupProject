package hoang.com.auction_system_be.service.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import hoang.com.auction_system_be.config.storage.ObjectStorageProperties;
import hoang.com.auction_system_be.dto.request.PresignImageUploadRequest;
import hoang.com.auction_system_be.dto.response.PresignedUploadResponse;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ObjectStorageService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final ObjectStorageProperties properties;

    public PresignedUploadResponse createImageUpload(Long userId, PresignImageUploadRequest request) {
        String contentType = normalizeContentType(request.contentType());
        validateImageUpload(contentType, request.fileSize());
        String objectKey = buildImageKey(userId, contentType);
        Duration duration = Duration.ofMinutes(properties.uploadUrlExpiryMinutes());
        PutObjectRequest put = PutObjectRequest.builder().bucket(properties.bucket()).key(objectKey)
                .contentType(contentType).contentLength(request.fileSize()).build();
        String url = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(duration).putObjectRequest(put).build()).url().toString();
        return new PresignedUploadResponse(objectKey, url, Instant.now().plus(duration));
    }

    public String uploadImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = normalizeContentType(file.getContentType());
        validateImageUpload(contentType, file.getSize());

        String objectKey = buildImageKey(userId, contentType);
        PutObjectRequest put = PutObjectRequest.builder().bucket(properties.bucket()).key(objectKey)
                .contentType(contentType).contentLength(file.getSize()).build();

        try {
            s3Client.putObject(put, RequestBody.fromBytes(file.getBytes()));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to upload image to object storage", exception);
        }
        return objectKey;
    }

    public void validateOwnedObject(Long userId, String objectKey) {
        if (objectKey == null || !objectKey.startsWith("items/" + userId + "/") || !objectExists(objectKey)) {
            throw new IllegalArgumentException("Invalid or incomplete image upload");
        }
    }

    public String resolveUrl(String value) {
        if (value == null || value.isBlank() || value.startsWith("http://") || value.startsWith("https://")
                || value.startsWith("/uploads/")) return value;
        if (properties.publicBaseUrl() != null && !properties.publicBaseUrl().isBlank()) {
            return properties.publicBaseUrl().replaceAll("/$", "") + "/" + value;
        }
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.downloadUrlExpiryMinutes()))
                .getObjectRequest(r -> r.bucket(properties.bucket()).key(value)).build()).url().toString();
    }

    public void deleteAfterCommit(String objectKey) {
        if (objectKey == null || !objectKey.startsWith("items/")) return;
        Runnable deletion = () -> s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.bucket()).key(objectKey).build());
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            deletion.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deletion.run();
            }
        });
    }

    private boolean objectExists(String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(properties.bucket()).key(objectKey).build());
            return true;
        } catch (NoSuchKeyException exception) {
            return false;
        } catch (software.amazon.awssdk.services.s3.model.S3Exception exception) {
            if (exception.statusCode() == 404) return false;
            throw exception;
        }
    }

    private String buildImageKey(Long userId, String contentType) {
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        return "items/" + userId + "/" + UUID.randomUUID() + extension;
    }

    private void validateImageUpload(String contentType, long fileSize) {
        if (!IMAGE_TYPES.contains(contentType) || fileSize > properties.maxImageSizeBytes()) {
            throw new IllegalArgumentException("Only JPG, PNG or WebP images up to 5 MB are allowed");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT);
    }
}
