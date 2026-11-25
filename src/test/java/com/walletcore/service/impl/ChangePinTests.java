package com.walletcore.service.impl;

import com.walletcore.dto.request.ChangePinRequest;
import com.walletcore.entity.User;
import com.walletcore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangePinTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UserServiceImpl userService = new UserServiceImpl(userRepository, passwordEncoder);
    private final UUID userId = UUID.randomUUID();

    @Test
    void changesPinOnLockedUserAndStoresOnlyHash() {
        User user = userWithPin();

        userService.changePin(userId, request("012345", "065432"));

        assertTrue(passwordEncoder.matches("065432", user.getPinHash()));
        assertFalse(passwordEncoder.matches("012345", user.getPinHash()));
        assertNotEquals("065432", user.getPinHash());
        verify(userRepository).findByIdForUpdate(userId);
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void incorrectOldPinLeavesHashUnchanged() {
        User user = userWithPin();
        String originalHash = user.getPinHash();

        assertStatus(HttpStatus.BAD_REQUEST, request("999999", "065432"));

        assertEquals(originalHash, user.getPinHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsSamePinWithoutUpdatingUser() {
        User user = userWithPin();
        String originalHash = user.getPinHash();

        assertStatus(HttpStatus.BAD_REQUEST, request("012345", "012345"));

        assertEquals(originalHash, user.getPinHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsUserWithoutPin() {
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(new User()));

        assertStatus(HttpStatus.CONFLICT, request("012345", "065432"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectsMissingUser() {
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.empty());

        assertStatus(HttpStatus.NOT_FOUND, request("012345", "065432"));

        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "12345", "1234567", "12a456", "１２３４５６"})
    void rejectsInvalidOldPinBeforeReadingDatabase(String pin) {
        assertStatus(HttpStatus.BAD_REQUEST, request(pin, "065432"));
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "12345", "1234567", "12a456", "１２３４５６"})
    void rejectsInvalidNewPinBeforeReadingDatabase(String pin) {
        assertStatus(HttpStatus.BAD_REQUEST, request("012345", pin));
        verifyNoInteractions(userRepository);
    }

    private User userWithPin() {
        User user = new User();
        user.setPinHash(passwordEncoder.encode("012345"));
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(user));
        return user;
    }

    private ChangePinRequest request(String oldPin, String newPin) {
        ChangePinRequest request = new ChangePinRequest();
        request.setOldPin(oldPin);
        request.setNewPin(newPin);
        return request;
    }

    private void assertStatus(HttpStatus status, ChangePinRequest request) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.changePin(userId, request));
        assertEquals(status, exception.getStatusCode());
    }
}
