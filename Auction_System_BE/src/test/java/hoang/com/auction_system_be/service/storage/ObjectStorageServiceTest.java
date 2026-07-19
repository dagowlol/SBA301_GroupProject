package hoang.com.auction_system_be.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import hoang.com.auction_system_be.config.storage.ObjectStorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ExtendWith(MockitoExtension.class)
class ObjectStorageServiceTest {

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner presigner;

    private ObjectStorageService service;

    @BeforeEach
    void setUp() {
        ObjectStorageProperties properties = new ObjectStorageProperties(
                "auction-images", "us-east-1", "http://localhost:9000",
                "minioadmin", "minioadmin", false,
                "http://localhost:9000/auction-images", 5, 60, 5_242_880);
        service = new ObjectStorageService(s3Client, presigner, properties);
    }

    @Test
    void uploadImageStoresObjectUnderCurrentUserPrefix() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        MockMultipartFile image = new MockMultipartFile(
                "image", "painting.png", "image/png", new byte[] { 1, 2, 3 });

        String key = service.uploadImage(42L, image);

        assertThat(key).startsWith("items/42/").endsWith(".png");
        ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(request.capture(), any(RequestBody.class));
        assertThat(request.getValue().bucket()).isEqualTo("auction-images");
        assertThat(request.getValue().key()).isEqualTo(key);
        assertThat(request.getValue().contentType()).isEqualTo("image/png");
        assertThat(request.getValue().contentLength()).isEqualTo(3);
    }

    @Test
    void uploadImageRejectsUnsupportedContentTypeBeforeCallingStorage() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "payload.svg", "image/svg+xml", new byte[] { 1 });

        assertThatThrownBy(() -> service.uploadImage(42L, file))
                .isInstanceOf(IllegalArgumentException.class);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void validateOwnedObjectChecksPrefixAndExistence() {
        String key = "items/42/7f6c4d1a-04f2-4f20-bca8-d722d714fe2f.jpg";
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        service.validateOwnedObject(42L, key);

        verify(s3Client).headObject(any(HeadObjectRequest.class));
        assertThatThrownBy(() -> service.validateOwnedObject(7L, key))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resolveUrlUsesConfiguredPublicBaseUrlAndKeepsLegacyUrls() {
        assertThat(service.resolveUrl("items/42/image.webp"))
                .isEqualTo("http://localhost:9000/auction-images/items/42/image.webp");
        assertThat(service.resolveUrl("/uploads/legacy.jpg")).isEqualTo("/uploads/legacy.jpg");
        assertThat(service.resolveUrl("https://cdn.example.com/image.jpg"))
                .isEqualTo("https://cdn.example.com/image.jpg");
    }
}
