package com.novablog.post.scheduler;

import com.novablog.post.service.PostService;
import com.novablog.tenant.model.Tenant;
import com.novablog.tenant.model.TenantStatus;
import com.novablog.tenant.repository.TenantRepository;
import com.novablog.multitenancy.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job that auto-publishes posts past their scheduled publish time.
 *
 * <p>Runs every minute, iterating over all active tenants and checking
 * for SCHEDULED posts that are due for publishing.</p>
 *
 * <p>Corresponds to US-007 acceptance criteria:
 * "Post auto-publishes at the set time; Spring @Scheduled job checks every minute"</p>
 */
@Slf4j
@Component
public class ScheduledPostPublisher {

    private final PostService postService;
    private final TenantRepository tenantRepository;

    public ScheduledPostPublisher(PostService postService, TenantRepository tenantRepository) {
        this.postService = postService;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Checks every minute for scheduled posts that are due for publishing.
     * Iterates over all active tenant schemas.
     */
    @Scheduled(fixedRate = 60_000) // Every 60 seconds
    public void publishScheduledPosts() {
        log.debug("Checking for scheduled posts due for publishing...");

        // Need to operate on public schema to list tenants
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        List<Tenant> activeTenants = tenantRepository.findByStatus(TenantStatus.ACTIVE);

        int totalPublished = 0;
        for (Tenant tenant : activeTenants) {
            try {
                TenantContext.setTenantId(tenant.getSchemaName());
                int published = postService.publishScheduledPosts();
                totalPublished += published;
            } catch (Exception e) {
                log.error("Error publishing scheduled posts for tenant {}: {}",
                        tenant.getSubdomain(), e.getMessage(), e);
            } finally {
                TenantContext.clear();
            }
        }

        if (totalPublished > 0) {
            log.info("Auto-published {} post(s) across {} tenant(s)",
                    totalPublished, activeTenants.size());
        }
    }
}
