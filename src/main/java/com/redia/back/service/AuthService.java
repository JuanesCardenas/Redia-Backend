package com.redia.back.service;

import com.redia.back.dto.*;
import com.redia.back.model.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio que define las operaciones relacionadas con autenticación.
 */
public interface AuthService {

    User register(RegisterRequestDTO request);

    User registerAdmin(RegisterRequestDTO request);

    void recoverPassword(PasswordRecoveryRequestDTO request);

    User findByEmail(String email);

    void changePassword(String userId, ChangePasswordDTO changePasswordDTO);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    void sendVerificationCode(ForgotPasswordDTO forgotPasswordDTO);

    AuthResponseDTO googleLogin(GoogleLoginRequestDTO request);

    java.util.Map<String, Object> completeProfile(String telefono, String password, MultipartFile foto, String email);

    // 2FA
    TwoFactorSetupResponseDTO setup2FA(String email);

    void enable2FA(String email, int code);

    void disable2FA(String email);

    AuthResponseDTO verifyTwoFactor(TwoFactorVerifyRequestDTO request);

    void requestUnsubscribe(String email);
}
