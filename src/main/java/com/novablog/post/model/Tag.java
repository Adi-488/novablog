package com.novablog.post.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a content tag within a tenant's schema.
 *
 * <p>Tags are shared across posts within a tenant and used for
 * categorization and filtering.</p>
 */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
}
