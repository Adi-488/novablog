package com.novablog.tenant.service;

import com.novablog.common.exception.SubdomainAlreadyExistsException;
import com.novablog.common.exception.TenantNotFoundException;
import com.novablog.multitenancy.TenantSchemaProvisioner;
import com.novablog.tenant.dto.TenantRegistrationRequest;
import com.novablog.tenant.dto.TenantResponse;
import com.novablog.tenant.model.Tenant;
import com.novablog.tenant.model.TenantStatus;
import com.novablog.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TenantService}.
 * Uses Mockito to isolate service logic from repository and provisioner.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantService")
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantSchemaProvisioner schemaProvisioner;

    private TenantService tenantService;

    private static final String SCHEMA_PREFIX = "tenant_";

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(tenantRepository, schemaProvisioner, SCHEMA_PREFIX);
    }

    // ----------------------------------------------------------------
    // registerTenant
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("registerTenant")
    class RegisterTenantTests {

        @Test
        @DisplayName("should register tenant successfully")
        void shouldRegisterTenantSuccessfully() {
            // Arrange
            TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                    .orgName("Acme Corp")
                    .subdomain("acme")
                    .logoUrl("https://example.com/logo.png")
                    .timezone("America/New_York")
                    .build();

            when(tenantRepository.existsBySubdomain("acme")).thenReturn(false);

            Tenant savedTenant = createTenant("acme", "Acme Corp", "tenant_acme");
            when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

            doNothing().when(schemaProvisioner).provisionTenantSchema("tenant_acme");

            // Act
            TenantResponse response = tenantService.registerTenant(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getOrgName()).isEqualTo("Acme Corp");
            assertThat(response.getSubdomain()).isEqualTo("acme");
            assertThat(response.getSchemaName()).isEqualTo("tenant_acme");
            assertThat(response.getStatus()).isEqualTo(TenantStatus.ACTIVE);
            assertThat(response.getWorkspaceUrl()).isEqualTo("https://acme.novablog.dev");

            // Verify provisioner was called
            verify(schemaProvisioner).provisionTenantSchema("tenant_acme");
        }

        @Test
        @DisplayName("should throw SubdomainAlreadyExistsException for duplicate subdomain")
        void shouldThrowOnDuplicateSubdomain() {
            TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                    .orgName("Acme Corp")
                    .subdomain("acme")
                    .build();

            when(tenantRepository.existsBySubdomain("acme")).thenReturn(true);

            assertThatThrownBy(() -> tenantService.registerTenant(request))
                    .isInstanceOf(SubdomainAlreadyExistsException.class)
                    .hasMessageContaining("acme");

            verify(tenantRepository, never()).save(any());
            verify(schemaProvisioner, never()).provisionTenantSchema(anyString());
        }

        @Test
        @DisplayName("should default timezone to UTC when not provided")
        void shouldDefaultTimezoneToUTC() {
            TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                    .orgName("Beta Inc")
                    .subdomain("beta")
                    .build();

            when(tenantRepository.existsBySubdomain("beta")).thenReturn(false);

            ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
            Tenant savedTenant = createTenant("beta", "Beta Inc", "tenant_beta");
            when(tenantRepository.save(captor.capture())).thenReturn(savedTenant);
            doNothing().when(schemaProvisioner).provisionTenantSchema("tenant_beta");

            tenantService.registerTenant(request);

            assertThat(captor.getValue().getTimezone()).isEqualTo("UTC");
        }

        @Test
        @DisplayName("should convert hyphens to underscores in schema name")
        void shouldConvertHyphensInSchemaName() {
            TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                    .orgName("Acme-Corp")
                    .subdomain("acme-corp")
                    .build();

            when(tenantRepository.existsBySubdomain("acme-corp")).thenReturn(false);

            Tenant savedTenant = createTenant("acme-corp", "Acme-Corp", "tenant_acme_corp");
            when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);
            doNothing().when(schemaProvisioner).provisionTenantSchema("tenant_acme_corp");

            TenantResponse response = tenantService.registerTenant(request);

            verify(schemaProvisioner).provisionTenantSchema("tenant_acme_corp");
        }

        @Test
        @DisplayName("should set tenant status to ACTIVE on registration")
        void shouldSetStatusToActive() {
            TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                    .orgName("Gamma LLC")
                    .subdomain("gamma")
                    .build();

            when(tenantRepository.existsBySubdomain("gamma")).thenReturn(false);

            ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
            Tenant savedTenant = createTenant("gamma", "Gamma LLC", "tenant_gamma");
            when(tenantRepository.save(captor.capture())).thenReturn(savedTenant);
            doNothing().when(schemaProvisioner).provisionTenantSchema("tenant_gamma");

            tenantService.registerTenant(request);

            assertThat(captor.getValue().getStatus()).isEqualTo(TenantStatus.ACTIVE);
        }
    }

    // ----------------------------------------------------------------
    // isSubdomainAvailable
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("isSubdomainAvailable")
    class SubdomainAvailabilityTests {

        @Test
        @DisplayName("should return true when subdomain is available")
        void shouldReturnTrueWhenAvailable() {
            when(tenantRepository.existsBySubdomain("neworg")).thenReturn(false);

            assertThat(tenantService.isSubdomainAvailable("neworg")).isTrue();
        }

        @Test
        @DisplayName("should return false when subdomain is taken")
        void shouldReturnFalseWhenTaken() {
            when(tenantRepository.existsBySubdomain("acme")).thenReturn(true);

            assertThat(tenantService.isSubdomainAvailable("acme")).isFalse();
        }
    }

    // ----------------------------------------------------------------
    // getTenantBySubdomain
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("getTenantBySubdomain")
    class GetTenantTests {

        @Test
        @DisplayName("should return tenant when found")
        void shouldReturnTenantWhenFound() {
            Tenant tenant = createTenant("acme", "Acme Corp", "tenant_acme");
            when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(tenant));

            TenantResponse response = tenantService.getTenantBySubdomain("acme");

            assertThat(response.getSubdomain()).isEqualTo("acme");
            assertThat(response.getOrgName()).isEqualTo("Acme Corp");
        }

        @Test
        @DisplayName("should throw TenantNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(tenantRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.getTenantBySubdomain("nonexistent"))
                    .isInstanceOf(TenantNotFoundException.class)
                    .hasMessageContaining("nonexistent");
        }
    }

    // ----------------------------------------------------------------
    // listTenants
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("listTenants")
    class ListTenantsTests {

        @Test
        @DisplayName("should list all tenants when status is null")
        void shouldListAllTenants() {
            List<Tenant> tenants = List.of(
                    createTenant("acme", "Acme Corp", "tenant_acme"),
                    createTenant("globex", "Globex Inc", "tenant_globex")
            );
            when(tenantRepository.findAll()).thenReturn(tenants);

            List<TenantResponse> responses = tenantService.listTenants(null);

            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("should filter tenants by status")
        void shouldFilterByStatus() {
            Tenant activeTenant = createTenant("acme", "Acme Corp", "tenant_acme");
            when(tenantRepository.findByStatus(TenantStatus.ACTIVE))
                    .thenReturn(List.of(activeTenant));

            List<TenantResponse> responses = tenantService.listTenants(TenantStatus.ACTIVE);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getSubdomain()).isEqualTo("acme");
        }
    }

    // ----------------------------------------------------------------
    // buildSchemaName
    // ----------------------------------------------------------------
    @Nested
    @DisplayName("buildSchemaName")
    class BuildSchemaNameTests {

        @Test
        @DisplayName("should build schema name with prefix")
        void shouldBuildSchemaName() {
            assertThat(tenantService.buildSchemaName("acme")).isEqualTo("tenant_acme");
        }

        @Test
        @DisplayName("should replace hyphens with underscores")
        void shouldReplaceHyphens() {
            assertThat(tenantService.buildSchemaName("acme-corp")).isEqualTo("tenant_acme_corp");
        }
    }

    // ----------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------
    private Tenant createTenant(String subdomain, String orgName, String schemaName) {
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .orgName(orgName)
                .subdomain(subdomain)
                .schemaName(schemaName)
                .status(TenantStatus.ACTIVE)
                .timezone("UTC")
                .build();
        tenant.setCreatedAt(OffsetDateTime.now());
        tenant.setUpdatedAt(OffsetDateTime.now());
        return tenant;
    }
}
