package com.collaberadigital.cove.utils;

import com.collaberadigital.cove.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public  class GenerateUtil {

    private static  UserRepository userRepository;

    @Autowired
    GenerateUtil(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public static String generateUniqueRegistrationId() {
        String  registrationId;
        do {
            registrationId = String.valueOf(new Random().nextLong(1000000000L, 9999999999L)); // Generates a number between 1 billion and 10 billion
        } while (userRepository.existsByRegistrationId(registrationId));
        return registrationId;
    }
}
