package com.carpool.app.security;

import com.carpool.app.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {
    public static User get() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
