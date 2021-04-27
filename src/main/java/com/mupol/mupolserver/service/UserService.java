package com.mupol.mupolserver.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {
    public boolean validateUsername(String username) {
        boolean isValidCharacter = Pattern.matches("^[가-힣0-9a-zA-Z-]*$", username);
        return 0 < username.length() && username.length() < 11 && isValidCharacter;
    }
}
