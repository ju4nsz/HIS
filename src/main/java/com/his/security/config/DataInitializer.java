package com.his.security.config;

import com.his.security.entity.Permiso;
import com.his.security.entity.Rol;
import com.his.security.entity.Usuario;
import com.his.security.repository.PermisoRepository;
import com.his.security.repository.RolRepository;
import com.his.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.his.security.util.Constants.EstadoUsuario.ACTIVO;
import static com.his.security.util.Constants.Roles.*;
import static com.his.security.util.Constants.Permisos.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {

            List<String> permisosIniciales = List.of(
                    PACIENTE_VER, PACIENTE_CREAR, PACIENTE_EDITAR,
                    CITA_EDITAR, CITA_CREAR, CITA_EDITAR, CITA_VER
            );

            for (String nombre : permisosIniciales) {
                permisoRepository.findByNombre(nombre).orElseGet(() -> {
                    Permiso p = Permiso.builder()
                            .nombre(nombre)
                            .descripcion("Permiso para " + nombre)
                            .build();
                    return permisoRepository.save(p);
                });
            }

            // 2️⃣ Crear roles si no existen
            Rol admin = rolRepository.findByNombreRol(ADMIN).orElseGet(() -> rolRepository.save(
                    Rol.builder().nombreRol(ADMIN).descripcion(ADMIN).build()
            ));
            Rol medico = rolRepository.findByNombreRol(MEDICO).orElseGet(() -> rolRepository.save(
                    Rol.builder().nombreRol(MEDICO).descripcion(MEDICO).build()
            ));
            Rol recepcionista = rolRepository.findByNombreRol(RECEPCIONISTA).orElseGet(() -> rolRepository.save(
                    Rol.builder().nombreRol(RECEPCIONISTA).descripcion(RECEPCIONISTA).build()
            ));

            Set<Permiso> todosPermisos = Set.copyOf(permisoRepository.findAll());
            admin.setPermisos(todosPermisos);
            rolRepository.save(admin);

            String emailAdmin = "ju4nsz@his.com";
            if (!usuarioRepository.existsByEmail(emailAdmin)) {
                Usuario adminUser = Usuario.builder()
                        .nombreCompleto("Juan Camilo")
                        .email(emailAdmin)
                        .passwordHash(passwordEncoder.encode("1234"))
                        .estado(ACTIVO)
                        .fechaCreacion(LocalDateTime.now())
                        .rol(admin)
                        .build();

                usuarioRepository.save(adminUser);
            }
        };
    }

}
