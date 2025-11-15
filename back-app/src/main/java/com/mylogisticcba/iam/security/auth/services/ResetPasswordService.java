package com.mylogisticcba.iam.security.auth.services;

import com.mylogisticcba.iam.security.auth.dtos.req.ConfirmResetPassRequest;
import com.mylogisticcba.iam.security.auth.dtos.req.SoliciteResetPassRequest;
import com.mylogisticcba.iam.security.auth.dtos.response.EncodedMailResponse;

public interface ResetPasswordService {
    EncodedMailResponse getMaskEmail(String username, String tenantName);
    void requestResetToken(SoliciteResetPassRequest soliciteResetPassRequest);
    void confirmResetPassword(ConfirmResetPassRequest request);
}
