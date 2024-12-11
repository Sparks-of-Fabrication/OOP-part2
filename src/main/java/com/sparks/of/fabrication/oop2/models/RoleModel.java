package com.sparks.of.fabrication.oop2.models;

import com.sparks.of.fabrication.oop2.users.Privileges;
import com.sparks.of.fabrication.oop2.users.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "role_model")
@NoArgsConstructor()
@Data
@Getter()
@Setter()
public class RoleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rolemodel", nullable = false)
    private Long id;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
}
