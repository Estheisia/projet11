package fr.uga.miashs.projetqcm.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Singleton
/**
 * Utilitaires pour la génération de JWT.
 * La clé privée est chargé à partir du fichier donné dans la propriété mp.jwt.decrypt.key.location.
 */
public class JWTUtils {

    @Inject
    @ConfigProperty(name = "mp.jwt.decrypt.key.location", defaultValue = "/privateKey.pem")
    private String privateKeyPath;

    @Inject
    @ConfigProperty(name = "mp.jwt.verify.issuermp.jwt.verify.issuer", defaultValue = "http://miashs.univ-grenoble-alpes.fr")
    private String issuer;

    private JWTAuth provider;


    @PostConstruct
    public void init() {
        String privateKey = readPemFile();
        provider = JWTAuth.create(null, new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("RS256")
                        .setBuffer(privateKey)
                ));
    }

    /**
     * Generate a Json Web Token with sub, groups and exp claims
     *
     * @param email    : the suject (sub)
     * @param roles    : the list of roles (groups)
     * @param validity : the validity of generated token in seconds
     * @return a base66 signed JWT
     */
    public String generateToken(String email, List<String> roles, int validity) {
        // doc https://vertx.io/docs/vertx-auth-jwt/java/
        JsonArray groups = new JsonArray();
        roles.forEach(r -> groups.add(r));

        JsonObject jwt = new JsonObject();
        // required by payara and recommended by 1.2
        jwt.put(Claims.sub.name(), email);
        //or/and
        // required 1.2
        jwt.put(Claims.upn.name(), email);
        // required 1.2
        jwt.put(Claims.exp.name(), Instant.now().plusSeconds(validity).getEpochSecond());
        // required 1.2
        jwt.put(Claims.iat.name(), Instant.now().getEpochSecond());
        //required 1.2
        jwt.put(Claims.iss.name(), issuer);

        jwt.put(Claims.groups.name(), groups);

        //required by payara and recommended by 1.2
        jwt.put(Claims.jti.name(), UUID.randomUUID().toString());

        return provider.generateToken(jwt, new JWTOptions().setAlgorithm("RS256"));
    }


    // NOTE:   Expected format is PKCS#8 (BEGIN PRIVATE KEY) NOT PKCS#1 (BEGIN RSA PRIVATE KEY)
    private String readPemFile() {
        StringBuilder sb = new StringBuilder(8192);
        try (BufferedReader is = new BufferedReader(
                new InputStreamReader(
                        JWTUtils.class.getResourceAsStream(privateKeyPath), StandardCharsets.US_ASCII))) {
            String line;
            while ((line = is.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}