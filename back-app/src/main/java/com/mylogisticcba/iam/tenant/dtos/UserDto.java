package com.mylogisticcba.iam.tenant.dtos;

import com.mylogisticcba.iam.tenant.enums.Role;
import com.mylogisticcba.iam.tenant.enums.UserStatus;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {
    private String id;
    private String username;
    private String email;
    private String telephone;
    private String address;
    private String city;
    private String stateOrProvince;
    List<Role> roles;
    boolean  owner;
    UserStatus status;
    private String vehicleId;
    private String vehiclePlate;


}
