package com.yovexa.solutions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a persisted refresh token for rotation and reuse detection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
@CompoundIndexes({
    @CompoundIndex(name = "family_revocation_idx", def = "{'familyId': 1, 'isRevoked': 1}"),
    @CompoundIndex(name = "admin_revocation_idx", def = "{'adminId': 1, 'isRevoked': 1}")
})
public class RefreshToken {

    @Id
    private String id;

    /**
     * SHA-256 hash of the presented refresh token. Unique index for O(1) lookups.
     */
    @Indexed(unique = true)
    private String tokenHash;

    /**
     * Identifies the rotation lineage / family. Every child token shares its ancestor's familyId.
     */
    private String familyId;

    /**
     * Identifier of the admin who owns this token.
     */
    private String adminId;

    /**
     * Email of the admin user for quick auditing.
     */
    private String adminEmail;

    /**
     * True once this token has been exchanged for a new child token in the rotation cycle.
     */
    @Builder.Default
    private boolean isUsed = false;

    /**
     * True if this token or its entire family was explicitly invalidated.
     */
    @Builder.Default
    private boolean isRevoked = false;

    /**
     * Reason for revocation (e.g. ROTATED, LOGOUT, REUSE_DETECTED, PASSWORD_CHANGED).
     */
    private String revokedReason;

    /**
     * Pointer to the child token hash that replaced this token.
     */
    private String replacedByTokenHash;

    @CreatedDate
    private Instant createdAt;

    /**
     * Timestamp when this token was consumed during rotation. Used for concurrency grace period.
     */
    private Instant usedAt;

    /**
     * TTL index: MongoDB automatically removes documents when Instant.now() exceeds expiresAt.
     */
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    /**
     * Telemetry metadata for security auditing.
     */
    private String createdByIp;

    private String userAgent;
}
