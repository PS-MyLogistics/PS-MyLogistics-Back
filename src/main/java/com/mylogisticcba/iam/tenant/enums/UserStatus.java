package com.mylogisticcba.iam.tenant.enums;

public enum UserStatus {
    PENDING_VERIFICATION, // cuando se registra pero no confirmo el email
    ACTIVE,               // cuando confirmo y ya puede usar el sistema
    SUSPENDED,            // bloqueado por alguna razón administrativa
    DELETED,              // dado de baja lógica
    FREEZED               //cuando puede estar bajo ataque

}
