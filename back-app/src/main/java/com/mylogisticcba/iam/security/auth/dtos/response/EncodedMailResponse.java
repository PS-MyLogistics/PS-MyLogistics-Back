package com.mylogisticcba.iam.security.auth.dtos.response;

import lombok.Data;

@Data
public class EncodedMailResponse {
    boolean success;
    String mailEncoded;
}
