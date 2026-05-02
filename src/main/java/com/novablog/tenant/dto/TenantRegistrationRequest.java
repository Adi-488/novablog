package com.novablog.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for tenant registration (US-001).
 *
 * <p>Validation rules:
 * <ul>
 *   <li>orgName: required, 2-255 characters</li>
 *   <li>subdomain: required, 3-63 chars, lowercase alphanumeric + hyphens, no leading/trailing hyphens</li>
 *   <li>timezone: optional, defaults to UTC</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 255, message = "Organization name must be between 2 and 255 characters")
    private String orgName;

    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 63, message = "Subdomain must be between 3 and 63 characters")
    @Pattern(
            regexp = "^[a-z][a-z0-9-]*[a-z0-9]$",
            message = "Subdomain must be lowercase, start with a letter, end with a letter/number, "
                    + "and contain only letters, numbers, and hyphens"
    )
    private String subdomain;

    @Size(max = 512, message = "Logo URL must be at most 512 characters")
    private String logoUrl;

    @Size(max = 50, message = "Timezone must be at most 50 characters")
    private String timezone;
}
