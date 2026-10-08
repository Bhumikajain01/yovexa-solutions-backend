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
@Schema(description = "Refresh token response containing the newly issued access token")
public class TokenRefreshResponse {

    @Schema(description = "Newly issued JWT access token")
    private String token;

    @Schema(description = "Newly rotated refresh token (also delivered via HttpOnly cookie)")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in seconds", example = "900")
    private long expiresIn;
}
