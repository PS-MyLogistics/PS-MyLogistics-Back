package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.PromotionEmailRequest;
import com.mylogisticcba.core.dto.response.PromotionEmailResponse;

public interface PromotionService {

    /**
     * Envía un email promocional a una lista de clientes
     * @param request Datos del email promocional
     * @return Resultado del envío (exitosos y fallidos)
     */
    PromotionEmailResponse sendPromotionalEmail(PromotionEmailRequest request);
}
