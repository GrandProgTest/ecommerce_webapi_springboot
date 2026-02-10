package com.finalproject.ecommerce.ecommerce.iam.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.CreateAddressCommand;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Entity
@Getter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String street;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String state;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String country;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String postalCode;

    @NotNull
    @Column(nullable = false)
    private Boolean isDefault;

    public Address() {
    }

    public Address(CreateAddressCommand command, User user) {
        this.user = user;
        this.street = command.street();
        this.city = command.city();
        this.state = command.state();
        this.country = command.country();
        this.postalCode = command.postalCode();
        this.isDefault = command.isDefault() != null ? command.isDefault() : false;
    }

    public void updateAddress(String street, String city, String state, String country, String postalCode) {
        if (street != null && !street.isBlank()) {
            this.street = street;
        }
        if (city != null && !city.isBlank()) {
            this.city = city;
        }
        if (state != null && !state.isBlank()) {
            this.state = state;
        }
        if (country != null && !country.isBlank()) {
            this.country = country;
        }
        if (postalCode != null && !postalCode.isBlank()) {
            this.postalCode = postalCode;
        }
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    public boolean belongsToUser(User user) {
        return this.user.equals(user);
    }

    public Long getUserId() {
        return this.user != null ? this.user.getId() : null;
    }
}
