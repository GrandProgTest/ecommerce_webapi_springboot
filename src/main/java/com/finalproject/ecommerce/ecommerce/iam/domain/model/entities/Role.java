package com.finalproject.ecommerce.ecommerce.iam.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Roles name;

    public Role(Roles name) {
        this.name = name;
    }

    public String getStringName() {
        return name.name();
    }

    public static Role getDefaultManagerRole() {
        return new Role(Roles.ROLE_MANAGER);
    }

    public static Role getDefaultClientRole() {
        return new Role(Roles.ROLE_CLIENT);
    }

    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

}