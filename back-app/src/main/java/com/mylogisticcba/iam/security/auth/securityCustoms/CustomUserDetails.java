package com.mylogisticcba.iam.security.auth.securityCustoms;

import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.tenant.enums.Role;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Data
public class CustomUserDetails implements UserDetails {
    private final String username;
    private final String password;
    private final UUID tenantId;
    private final UUID globalTokenVersion;
    private final UUID sessionId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final UserEntity user;

    public  CustomUserDetails(UserEntity user,UUID sessionID){
        this.user = user;
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.tenantId = user.getTenant().getId();
        this.authorities = this.getListGrantedAuth(user.getRoles());
        this.globalTokenVersion = user.getGlobalTokenVersion();
        this.sessionId=sessionID;
    }


    private List<SimpleGrantedAuthority> getListGrantedAuth(List<Role>roles){
        var list = new ArrayList<SimpleGrantedAuthority>();
        roles.forEach(role-> list.add(new SimpleGrantedAuthority(role.getAuthorityName())));
        return list;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
