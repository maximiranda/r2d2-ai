package io.slingr.endpoints.r2d2;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.slingr.endpoints.HttpEndpoint;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.framework.annotations.*;
import io.slingr.endpoints.services.AppLogs;
import io.slingr.endpoints.services.datastores.DataStore;
import io.slingr.endpoints.services.rest.RestClient;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.ws.exchange.FunctionRequest;
import org.slf4j.Logger;



import javax.ws.rs.core.Form;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
/**
 * R2D2-AI endpoint
 * Created by maximiranda on 29/05/23.
 */
@SlingrEndpoint(name = "r2d2ai", functionPrefix = "_")
public class R2D2AiEndpoint extends HttpEndpoint {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(R2D2AiEndpoint.class);

    private static final String BASE_URL = "https://us-central1-aiplatform.googleapis.com/v1/projects/slingr-ai/locations/us-central1/publishers/google/models/";


    @EndpointProperty
    private String serviceAccountEmail;
    @EndpointDataStore
    private DataStore googleAccessToken;
    @ApplicationLogger
    private AppLogs appLogger;
    @EndpointConfiguration
    private Json configuration;
    @Override
    public String getApiUri() { return BASE_URL; }

    private PrivateKey privateKey;
    private String accessToken;
    public static final String DEFAULT_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    public static final String LAST_TOKEN = "_LAST_TOKEN";
    public static final String TOKEN_DURATION = "tokenDuration";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String ID = "_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String GOOGLE_AUTH_URL = "https://oauth2.googleapis.com/token";
    public static final String AUTH_URL_SNIPPET = "https://www.googleapis.com/auth/";

    @Override
    public void endpointStarted() {
        try {
            appLogger.info("Setting access token on endpoint start....");
            String privateKey = this.configuration.string("privateKey");
            try {
                String privateKeyContent = privateKey.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
                PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
                KeyFactory kf = KeyFactory.getInstance("RSA");
                this.privateKey = kf.generatePrivate(keySpecPKCS8);
            } catch (Exception e) { appLogger.error("An error occurred while generating the private key ", e);}
            //the needed scopes must be joined with a space character (' ') in order to pass it to the JWT about to be generated
            try { googleAccessToken.removeById(LAST_TOKEN); }
            finally { setAccessToken(); }
            appLogger.info("Access token successfully set up on endpoint start....");
        } catch (Exception e) {
            appLogger.error("An error occurred while setting the access token ", e);
        }

        logger.error("Endpoint started");
        appLogger.error("Endpoint started");
        httpService().setAllowExternalUrl(true);
    }

    @EndpointFunction(name = "_get")
    public Json get(FunctionRequest request) {
        try { return defaultGetRequest(request); }
        catch (EndpointException restException) {
            if (checkInvalidTokenError(restException)) {
                setAccessToken();
                return defaultGetRequest(request);
            }
            throw restException;
        }
    }

    @EndpointFunction(name = "_post")
    public Json post(FunctionRequest request) {
        try{ return defaultPostRequest(request); }
        catch (EndpointException restException) {
            if (checkInvalidTokenError(restException)) {
                setAccessToken();
                return defaultPostRequest(request);
            }
            throw restException;
        }
    }


/*    private boolean handleErrorCodes(EndpointException restException) {
        if (restException.getHttpStatusCode() == 401) {
            appLogger.error("401 - Invalid Authentication or 401 - Incorrect API key provided or 401 - You must be a member of an organization to use the API.");
            return false;
        }
        if (restException.getHttpStatusCode() == 429) {
            appLogger.error("429 - Rate limit reached for requests or 429 - You exceeded your current quota, please check your plan and billing details or 429 - The engine is currently overloaded, please try again later.");
            return true;
        }
        if (restException.getHttpStatusCode() == 500) {
            appLogger.error("500 - The server had an error while processing your request.");
            return true;
        }
        return false;
    }*/
    private boolean checkInvalidTokenError(Exception e) {
        if (e instanceof EndpointException) {
            EndpointException restException = (EndpointException) e;
            if (restException.getCode() != null) logger.error("Status Code: "+ restException.getReturnCode());
            return restException.getReturnCode() == 401;
        }
        return false;
    }
    public void setAccessToken() {
        appLogger.info("Generating JWT....");
        long now = System.currentTimeMillis();
        Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) this.privateKey);
        String jwt =  JWT.create()
                .withIssuer(this.serviceAccountEmail)
                .withClaim("scope", DEFAULT_SCOPE)
                .withAudience(GOOGLE_AUTH_URL)
                .withExpiresAt(new Date(now + 60 * 1000L)) // 60 seconds duration
                .withIssuedAt(new Date(now))
                .sign(algorithm);
        appLogger.info("JWT generated successfully! ");
        Form formBody = new Form().param("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer").param("assertion", jwt);
        try {
            Json accessTokenResponse = RestClient.builder(GOOGLE_AUTH_URL)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody);
            appLogger.info("Access token retrieved from JWT!");

            Json newToken = Json.map()
                    .set(ACCESS_TOKEN, accessTokenResponse.string("access_token"))
                    .set(TIMESTAMP, System.currentTimeMillis())
                    .set(TOKEN_DURATION, accessTokenResponse.string("expires_in"))
                    .set(ID, LAST_TOKEN);
            this.googleAccessToken.save(newToken);
            this.accessToken = accessTokenResponse.string("access_token");
        } catch (Exception e) {
            appLogger.error("An error occurred while trying to get the access token, check the given OAuth scopes", e);
        }
        httpService().setupBearerAuthenticationHeader(this.accessToken);
        httpService().setupDefaultHeader("Content-Type", "application/json");
    }
}