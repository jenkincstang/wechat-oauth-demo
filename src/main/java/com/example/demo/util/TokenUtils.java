package com.example.demo.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Calendar;
import java.util.Map;

/**
 * @author Jenkin
 * @date 2020/9/1 - 0:52
 */
public class TokenUtils {
  // token秘钥
  private static final String TOKEN_SECRET = "@!QwtdjdYADGSHGD";

  public static String getToken(Map<String, String> map) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, 20);
    Builder builder = JWT.create();
    map.forEach(
        (key, value) -> {
          builder.withClaim(key, value);
        });
    String token = builder.withExpiresAt(calendar.getTime()).sign(Algorithm.HMAC256(TOKEN_SECRET));
    return token;
  }

  public static DecodedJWT verify(String token) {
    Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
    JWTVerifier verifier = JWT.require(algorithm).build();
    DecodedJWT jwt = verifier.verify(token);
    return jwt;
  }
  }
