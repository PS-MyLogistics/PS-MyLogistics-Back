package com.mylogisticcba.iam.security.auth.securityCustoms;

import java.util.UUID;

public class TenantContextHolder {

        private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

        public static void setTenant(UUID tenantId) {
            currentTenant.set(tenantId);
        }

        public static UUID getTenant() {
            return currentTenant.get();
        }

        public static void clear() {
            currentTenant.remove();
        }


}
