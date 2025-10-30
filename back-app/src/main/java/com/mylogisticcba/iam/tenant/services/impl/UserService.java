package com.mylogisticcba.iam.tenant.services.impl;

import com.mylogisticcba.iam.events.UserInTenantCreatedEvent;
import com.mylogisticcba.iam.repositories.TenantRepository;
import com.mylogisticcba.iam.repositories.UserRepository;
import com.mylogisticcba.iam.repositories.VerificarionTokenRepository;
import com.mylogisticcba.iam.security.auth.dtos.req.RegisterOwnerRequest;
import com.mylogisticcba.iam.security.auth.entity.VerificationToken;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.iam.tenant.dtos.EditUserInTenantRequest;
import com.mylogisticcba.iam.tenant.dtos.RegisterUserInTenantRequest;
import com.mylogisticcba.iam.tenant.dtos.UserDto;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.tenant.enums.Role;
import com.mylogisticcba.iam.tenant.enums.UserStatus;
import com.mylogisticcba.iam.tenant.exceptions.UserServiceException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService implements com.mylogisticcba.iam.tenant.services.UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private  final VerificarionTokenRepository verificarionTokenRepository;

    @Qualifier("defaultModelMapper")
    private final ModelMapper modelMapper;


    public UserEntity createUserOwner(RegisterOwnerRequest req, TenantEntity tenant) {

        if(userRepository.existsByUsernameAndOwnerTrue(req.getUsername())) {
            throw new UserServiceException("Username already exists globally");
        }
        UserEntity user = new UserEntity();
        user.setTenant(tenant);
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRoles(List.of(Role.OWNER, Role.ADMIN));
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setOwner(true);
        user.setTelephone(req.getTelephone());
        user.setAddress(req.getAddress());
        user.setCity(req.getCity());
        user.setStateOrProvince(req.getStateOrProvince());

        return userRepository.save(user);

    }


    @Transactional
    public UserDto createUserInTenant(RegisterUserInTenantRequest request) {

        UUID tenantId = TenantContextHolder.getTenant();
        if(userRepository.existsByUsernameAndTenant_Id(request.getUsername(), tenantId)) {
            throw new UserServiceException("Username already exists in tenant");
        }
        if(userRepository.existsByEmailAndTenant_Id(request.getEmail(), tenantId)) {
            throw new UserServiceException("Email already exists in tenant");
        }

        UserEntity user = new UserEntity();
        TenantEntity tenant = tenantRepository.findById(tenantId).orElseThrow(()-> new UserServiceException("Tenant not found"));
        user.setTenant(tenant);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(request.getRoles());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setOwner(false);
        user.setTelephone(request.getTelephone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setStateOrProvince(request.getStateOrProvince());

        UserDto dto=  modelMapper.map(saveUserInTenant(user),UserDto.class);

        VerificationToken vToken = verificarionTokenRepository.save(VerificationToken.builder()
                                .id(UUID.randomUUID())
                                .token(UUID.randomUUID())
                                .expiryDate(Instant.now().plus(2, ChronoUnit.DAYS))
                                .tenantId(tenantId)
                                .userId(user.getId())
                                .build());

        VerificationToken vTokenSaved = verificarionTokenRepository.save(vToken);

        UserInTenantCreatedEvent event=  UserInTenantCreatedEvent.builder()
                .email(dto.getEmail())
                .phone(dto.getTelephone())
                .tenantName(user.getTenant().getName())
                .tokenVerification(vTokenSaved.getToken())
                .username(user.getUsername())
                .tenantId(user.getTenant().getId())
                .build();

        applicationEventPublisher.publishEvent(event);

        return dto;
    }
    @Transactional
    public UserDto editUserDealerInTenant( EditUserInTenantRequest request) {

        UUID tenantId = TenantContextHolder.getTenant();

        // Buscar el usuario existente
        UserEntity user = userRepository.findByIdAndTenant_Id(request.getUserId(), tenantId)
                .orElseThrow(() -> new UserServiceException("User not found in tenant"));
        if(user.isOwner()|| user.getRoles().contains(Role.OWNER)) {
            throw new UserServiceException("Cannot edit owner user");
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            throw new UserServiceException("Cannot edit owner user");
        }
        if (user.getRoles().contains(Role.SUPERADMIN)) {
            throw new UserServiceException("Cannot edit superAdmin user");
        }

        // Validar username si cambió
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsernameAndTenant_Id(request.getUsername(), tenantId)) {
            throw new UserServiceException("Username already exists in tenant");
        }

        // Actualizar campos
        user.setUsername(request.getUsername());
        user.setRoles(request.getRoles());
        user.setTelephone(request.getTelephone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setStateOrProvince(request.getStateOrProvince());
        if(request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        UserDto dto = modelMapper.map(userRepository.save(user), UserDto.class);

        return dto;
    }

    public List<UserDto> getUsersByTenant() {
        UUID tenantId = TenantContextHolder.getTenant();
        List<UserEntity>users = userRepository.findByTenant_Id(tenantId).orElseThrow(()
                -> new EntityNotFoundException("users not found"));

        List<UserDto> userDtos = new ArrayList<>();
        users.forEach(user ->userDtos.add(modelMapper.map(user, UserDto.class)) );
        return userDtos;
    }


    public UserEntity getUserByIdAndTenantId(UUID userId, UUID tenantId) {
        return  userRepository.findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() -> new UserServiceException("Not found user in this space") );
    }

    public UserEntity getUserByEmailAndTenantId(String email, UUID tenantId) {
        return userRepository.findByEmailAndTenant_Id(email, tenantId).orElseThrow(
        ()->new UserServiceException("Not found user in this space")
        );
    }

    public UserEntity getUserByUsernameAndTenant(String username,UUID tenanId){
        return  userRepository.findByUsernameAndTenant_Id(username,tenanId).orElseThrow(
                ()-> new UserServiceException("Not found user in this space")
        );
    }
    public UserEntity getUserByUsernameAndEmailAndTenantId(String user , String email, UUID tenanId){

        return userRepository.findByUsernameAndEmailAndTenant_Id(user,email,tenanId).orElseThrow(
                ()-> new UserServiceException("user not found"));

    }


    public UserEntity changePassword(UserEntity user, String newPassword) {

        user.setPassword(passwordEncoder.encode(newPassword));
        return user;
    }

    private UserEntity saveUserInTenant(UserEntity user) {
        if(userRepository.existsByUsernameAndTenant_Id(user.getUsername(), user.getTenant().getId())) {
            throw new RuntimeException("Username already exists in this tenant");
        }
        return userRepository.save(user);
    }




}
