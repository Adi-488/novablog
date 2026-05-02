package com.novablog.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO for subdomain availability check.
 */
@Data
@AllArgsConstructor
public class SubdomainAvailabilityResponse {

    private String subdomain;
    private boolean available;
}
