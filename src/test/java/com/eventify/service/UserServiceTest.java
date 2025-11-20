package com.eventify.service;

import com.eventify.dto.UserRegistrationDto;
import com.eventify.exception.UsernameAlreadyExistsException;
import com.eventify.model.User;
import com.eventify.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("ROLE_USER");

        registrationDto = new UserRegistrationDto();
        registrationDto.setName("New User");
        registrationDto.setEmail("new@example.com");
        registrationDto.setPassword("password123");
    }

    @Test
    void registerUser_ShouldCreateNewUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerUser(registrationDto);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void registerUser_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.registerUser(registrationDto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByEmail_WhenExists_ShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User result = userService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("Test User", result.getName());
    }

    @Test
    void findByEmail_WhenNotExists_ShouldThrowException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.findByEmail("notfound@example.com");
        });
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void updateUserRole_WithValidRole_ShouldUpdateRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUserRole(1L, "ROLE_ORGANIZER");

        assertEquals("ROLE_ORGANIZER", result.getRole());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserRole_WithoutRolePrefix_ShouldAddPrefix() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUserRole(1L, "ADMIN");

        assertEquals("ROLE_ADMIN", testUser.getRole());
    }

    @Test
    void updateUserRole_WithInvalidRole_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserRole(1L, "INVALID_ROLE");
        });
    }

    @Test
    void updateUserRole_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.updateUserRole(99L, "ROLE_ADMIN");
        });
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrowException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(99L);
        });

        verify(userRepository, never()).deleteById(anyLong());
    }
}
