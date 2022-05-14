package ru.gameTheory.server.utils;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;
import java.util.Map;

public class JWT {
    private static Algorithm getAlgorithm() {
        return Algorithm.HMAC256("education-front-secret-key=11223344");
    }

    public interface CreateJWTProcessCallback {
        void call(JWTCreator.Builder builder);
    }

    public static String createJWT(CreateJWTProcessCallback callback) {
        try {
            Algorithm algorithm = getAlgorithm();
            final JWTCreator.Builder builder = com.auth0.jwt.JWT.create();
            callback.call(builder);
            return builder.sign(algorithm);
        } catch (JWTCreationException exception){
            return null;
        }
    }

    public static Map<String, Claim> getJWTClaims(final String token) {
        JWTVerifier verifier = com.auth0.jwt.JWT.require(getAlgorithm())
                .acceptExpiresAt(new Date().getTime())
                .build();
        final DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getClaims();
    }

    public static Integer getUserIdByToken(final String token) {
        JWTVerifier verifier = com.auth0.jwt.JWT.require(getAlgorithm())
                .acceptExpiresAt(new Date().getTime())
                .build();
        final DecodedJWT decodedJWT = verifier.verify(token);
        return Integer.parseInt(decodedJWT.getClaim("userId").asString());
    }
}
