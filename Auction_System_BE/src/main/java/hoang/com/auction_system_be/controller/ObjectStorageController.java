package hoang.com.auction_system_be.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hoang.com.auction_system_be.dto.request.PresignImageUploadRequest;
import hoang.com.auction_system_be.dto.response.ApiResponse;
import hoang.com.auction_system_be.dto.response.PresignedUploadResponse;
import hoang.com.auction_system_be.service.auth.SecurityContextService;
import hoang.com.auction_system_be.service.storage.ObjectStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class ObjectStorageController {
    private final ObjectStorageService objectStorageService;
    private final SecurityContextService securityContextService;

    @PostMapping("/images/presign")
    public ApiResponse<PresignedUploadResponse> presignImage(@RequestBody @Valid PresignImageUploadRequest request) {
        Long userId = securityContextService.getCurrentUserEntity().getId();
        return ApiResponse.<PresignedUploadResponse>builder()
                .result(objectStorageService.createImageUpload(userId, request)).build();
    }
}
