package com.yaoyao.service;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(String username, String password);

    Map<String, Object> getMe(String token);
}
