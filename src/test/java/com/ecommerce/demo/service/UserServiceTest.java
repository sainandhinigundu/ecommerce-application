package com.ecommerce.demo.service;

import com.ecommerce.demo.exception.DuplicateEmailException;
import com.ecommerce.demo.exception.ResourceNotFoundException;
import com.ecommerce.demo.model.User;
import com.ecommerce.demo.repository.UserRepository;
import com.ecommerce.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice Sharma", "alice@example.com", "secret123", "CUSTOMER");
    }

    @Test
    @DisplayName("Registers a new user when email is not already taken")
    void register_success() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.register(user);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Throws DuplicateEmailException when email is already registered")
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(user.getEmail());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Returns user when found by id")
    void getById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertThat(result.getName()).isEqualTo("Alice Sharma");
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when user id does not exist")
    void getById_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Returns user when found by email")
    void getByEmail_found() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        User result = userService.getByEmail("alice@example.com");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when email does not exist")
    void getByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Returns all registered users")
    void getAll_returnsList() {
        User second = new User(2L, "Bob Rao", "bob@example.com", "pass456", "CUSTOMER");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, second));

        List<User> result = userService.getAll();

        assertThat(result).hasSize(2).extracting(User::getEmail)
                .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    @Test
    @DisplayName("Updates an existing user's name and email")
    void update_success() {
        User updated = new User(null, "Alice Updated", "alice.new@example.com", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.update(1L, updated);

        assertThat(result.getName()).isEqualTo("Alice Updated");
        assertThat(result.getEmail()).isEqualTo("alice.new@example.com");
    }

    @Test
    @DisplayName("Update throws ResourceNotFoundException for unknown user id")
    void update_userNotFound_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(42L, user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deletes a user that exists")
    void delete_success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Delete throws ResourceNotFoundException when user does not exist")
    void delete_userNotFound_throwsException() {
        when(userRepository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(42L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }
}
