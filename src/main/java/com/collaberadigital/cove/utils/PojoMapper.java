package com.collaberadigital.cove.utils;

import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.model.AuthUser;

public class PojoMapper {

    public static AuthUser toUserRecord(UserEntity entity){
        return new AuthUser(
                entity.getUserId(),
                entity.getFirstname(),
                entity.getLastname(),
                entity.getEmail(),
                entity.getCompany(),
                entity.getDesignation(),
                entity.getCountry()
        );
    }

}


