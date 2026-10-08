package com.yovexa.solutions.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request payload (optional if passed via HttpOnly cookie)")
public class TokenRefreshRequest {

    @Schema(description = "The plain text refresh token", example = "d9f8e7b6a5...")
    private String refreshToken;
}
