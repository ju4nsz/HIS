package com.his.security.service;

import com.his.security.dto.LogInRequest;
import com.his.security.dto.RegisterRequest;
import com.his.security.entity.Rol;
import com.his.security.entity.Usuario;
import com.his.security.exception.ApiException;
import com.his.security.repository.RolRepository;
import com.his.security.repository.UsuarioRepository;
import com.his.security.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_ShouldSave_WhenUserDoesNotExist() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.com");
        request.setPassword("123456");
        request.setNombreCompleto("Usuario Test");

        Rol rolRecep = Rol.builder().nombreRol(Constants.Roles.RECEPCIONISTA).build();

        when(usuarioRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(rolRepository.findByNombreRol(Constants.Roles.RECEPCIONISTA)).thenReturn(Optional.of(rolRecep));
        when(passwordEncoder.encode("123456")).thenReturn("hashed123");

        // Act
        authService.registerUser(request);

        // Assert
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario saved = captor.getValue();
        assertEquals("test@mail.com", saved.getEmail());
        assertEquals("hashed123", saved.getPasswordHash());
        assertEquals(Constants.Roles.RECEPCIONISTA, saved.getRol().getNombreRol());
    }

    @Test
    void registerUser_ShouldThrow_WhenUserAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@mail.com");

        when(usuarioRepository.existsByEmail("test@mail.com")).thenReturn(true);

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> authService.registerUser(request));
        assertEquals(Constants.Mensajes.USUARIO_EXISTENTE, ex.getMessage());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        String email = "test@mail.com";
        String password = "123456";

        Usuario usuario = Usuario.builder()
                .email(email)
                .passwordHash("hashed123")
                .rol(Rol.builder().nombreRol(Constants.Roles.RECEPCIONISTA).build())
                .build();

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(password, "hashed123")).thenReturn(true);

        // Act
        var response = authService.login(new LogInRequest(email, password));

        // Assert
        assertNotNull(response.getAccesToken());
        assertEquals(email, response.getEmail());
    }

    @Test
    void login_ShouldThrow_WhenUserNotFound() {
        when(usuarioRepository.findByEmail("notfound@mail.com")).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> authService.login(new LogInRequest("notfound@mail.com", "123")));
    }

    @Test
    void login_ShouldThrow_WhenPasswordIsInvalid() {
        Usuario usuario = Usuario.builder()
                .email("test@mail.com")
                .passwordHash("hashed123")
                .rol(Rol.builder().nombreRol(Constants.Roles.RECEPCIONISTA).build())
                .build();

        when(usuarioRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpass", "hashed123")).thenReturn(false);

        assertThrows(ApiException.class, () -> authService.login(new LogInRequest("test@mail.com", "wrongpass")));
    }


}
