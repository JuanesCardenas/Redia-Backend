package com.redia.back.service;

import com.redia.back.dto.*;
import com.redia.back.model.User;

/**
 * Servicio que define las operaciones relacionadas con autenticación.
 */
public interface AuthService {

    User register(RegisterRequestDTO request);

    void recoverPassword(PasswordRecoveryRequestDTO request);

    User findByEmail(String email);

    void changePassword(String userId, ChangePasswordDTO changePasswordDTO);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    void sendVerificationCode(ForgotPasswordDTO forgotPasswordDTO);

    AuthResponseDTO googleLogin(GoogleLoginRequestDTO request);
}