package com.sparks.of.fabrication.oop2.users;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {
    CASHIER(Set.of(Privileges.CASHIER)),
    MANAGER(Set.of(Privileges.MANAGE_INVENTORY, Privileges.VIEW_REPORTS)),
    ADMIN(Set.of(Privileges.ADMIN_PRIVILEGES, Privileges.VIEW_REPORTS));

    private final Set<Privileges> privileges;

    Role(Set<Privileges> privileges) {
        this.privileges = privileges;
    }

}
