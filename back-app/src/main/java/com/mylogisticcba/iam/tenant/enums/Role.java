package com.mylogisticcba.iam.tenant.enums;


public enum Role {
    SUPERADMIN,
    OWNER,
    ADMIN,
    DEALER;



    //Spring Security  espera que los roles se llamen con prefijo ROLE_
    public String getAuthorityName() {
        return "ROLE_" + this.name();
    }
}
