package com.novablog.tenant.dto;

import com.novablog.tenant.model.Tenant;
import com.novablog.tenant.model.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for tenant information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private UUID id;
    private String orgName;
    private String subdomain;
    private String schemaName;
    private String logoUrl;
    private TenantStatus status;
    private String timezone;
    private String workspaceUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Maps a Tenant entity to a TenantResponse DTO.
     *
     * @param tenant the tenant entity
     * @return the response DTO
     */
    public static TenantResponse fromEntity(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .orgName(tenant.getOrgName())
                .subdomain(tenant.getSubdomain())
                .schemaName(tenant.getSchemaName())
                .logoUrl(tenant.getLogoUrl())
                .status(tenant.getStatus())
                .timezone(tenant.getTimezone())
                .workspaceUrl("https://" + tenant.getSubdomain() + ".novablog.dev")
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
