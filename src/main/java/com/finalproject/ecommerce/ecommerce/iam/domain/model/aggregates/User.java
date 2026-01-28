package com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AuditableAbstractAggregateRoot<User> {

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }
}
