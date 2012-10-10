package io.cinderella.security;

import com.cloud.bridge.service.UserContext;
import com.cloud.bridge.util.EC2RestAuth;
import io.cinderella.exception.PermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

/**
 * @author shane
 * @since 9/26/12
 */
@Component
@PropertySource("file:${user.home}/.cinderella/ec2-service.properties")
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Autowired
    Environment env;

    /**
     * This function implements the EC2 REST authentication algorithm. It uses
     * the given "AWSAccessKeyId" parameter to look up the Cloud.com account
     * holder's secret key which is used as input to the signature calculation.
     * In addition, it tests the given "Expires" parameter to see if the
     * signature has expired and if so the request fails.
     */
    @Override
    public boolean authenticateRequest(HttpServletRequest request,
                                       HttpServletResponse response) throws SignatureException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException {


        String cloudAccessKey = null;
        String signature = null;
        String sigMethod = null;

        // [A] Basic parameters required for an authenticated rest request
        // -> note that the Servlet engine will un-URL encode all parameters we
        // extract via "getParameterValues()" calls
        String[] awsAccess = request.getParameterValues("AWSAccessKeyId");
        if (null != awsAccess && 0 < awsAccess.length)
            cloudAccessKey = awsAccess[0];
        else {
            response.sendError(530, "Missing AWSAccessKeyId parameter");
            return false;
        }

        String[] clientSig = request.getParameterValues("Signature");
        if (null != clientSig && 0 < clientSig.length)
            signature = clientSig[0];
        else {
            response.sendError(530, "Missing Signature parameter");
            return false;
        }

        String[] method = request.getParameterValues("SignatureMethod");
        if (null != method && 0 < method.length) {
            sigMethod = method[0];
            if (!sigMethod.equals("HmacSHA256") && !sigMethod.equals("HmacSHA1")) {
                response.sendError(531, "Unsupported SignatureMethod value: " + sigMethod
                        + " expecting: HmacSHA256 or HmacSHA1");
                return false;
            }
        } else {
            response.sendError(530, "Missing SignatureMethod parameter");
            return false;
        }

      /*
       * String[] version = request.getParameterValues( "Version" ); if ( null
       * != version && 0 < version.length ) { if (!version[0].equals(
       * wsdlVersion )) { response.sendError(531, "Unsupported Version value: "
       * + version[0] + " expecting: " + wsdlVersion ); return false; } } else {
       * response.sendError(530, "Missing Version parameter" ); return false; }
       */

        String[] sigVersion = request.getParameterValues("SignatureVersion");
        if (null != sigVersion && 0 < sigVersion.length) {
            if (!sigVersion[0].equals("2")) {
                response.sendError(531, "Unsupported SignatureVersion value: " + sigVersion[0] + " expecting: 2");
                return false;
            }
        } else {
            response.sendError(530, "Missing SignatureVersion parameter");
            return false;
        }

        // -> can have only one but not both { Expires | Timestamp } headers
        String[] expires = request.getParameterValues("Expires");
        if (null != expires && 0 < expires.length) {
            // -> contains the date and time at which the signature included in the
            // request EXPIRES
            if (hasSignatureExpired(expires[0])) {
                response.sendError(531, "Expires parameter indicates signature has expired: " + expires[0]);
                return false;
            }
        } else { // -> contains the date and time at which the request is SIGNED
            String[] time = request.getParameterValues("Timestamp");
            if (null == time || 0 == time.length) {
                response.sendError(530, "Missing Timestamp and Expires parameter, one is required");
                return false;
            }
        }

        // [B] Use the cloudAccessKey to get the users secret key in the db
        // UserCredentialsDao credentialDao = new UserCredentialsDao();
      /*
       * UserCredentials cloudKeys = credentialDao.getByAccessKey(
       * cloudAccessKey ); if ( null == cloudKeys ) { logger.debug(
       * cloudAccessKey +
       * " is not defined in the EC2 service - call SetUserKeys" );
       * response.sendError(404, cloudAccessKey +
       * " is not defined in the EC2 service - call SetUserKeys" ); return
       * false; } else cloudSecretKey = cloudKeys.getSecretKey();
       */
//        cloudSecretKey = ec2properties.getProperty("key." + cloudAccessKey);
        String cloudSecretKey = env.getProperty("key." + cloudAccessKey);
        if (null == cloudSecretKey) {
            throw new PermissionDeniedException("No secret key configured for access key: "+ cloudAccessKey);
        }

        // [C] Verify the signature
        // -> getting the query-string in this way maintains its URL encoding
        EC2RestAuth restAuth = new EC2RestAuth();
        restAuth.setHostHeader(request.getHeader("Host"));
        String requestUri = request.getRequestURI();

        // If forwarded from another basepath:
        String forwardedPath = (String) request.getAttribute("javax.servlet.forward.request_uri");
        if (forwardedPath != null) {
            requestUri = forwardedPath;
        }
        restAuth.setHTTPRequestURI(requestUri);

        String queryString = request.getQueryString();
        // getQueryString returns null (does it ever NOT return null for these),
        // we need to construct queryString to avoid changing the auth code...
        if (queryString == null) {
            // construct our idea of a queryString with parameters!
            Enumeration<?> params = request.getParameterNames();
            if (params != null) {
                while (params.hasMoreElements()) {
                    String paramName = (String) params.nextElement();
                    // exclude the signature string obviously. ;)
                    if (paramName.equalsIgnoreCase("Signature"))
                        continue;
                    if (queryString == null)
                        queryString = paramName + "=" + URLEncoder.encode(request.getParameter(paramName), "UTF-8").
                                replace("+", "%20").
                                replace("*", "%2A").
                                replace("%7E", "~");
                    else
                        queryString = queryString + "&" + paramName + "="
                                + URLEncoder.encode(request.getParameter(paramName), "UTF-8").
                                replace("+", "%20").
                                replace("*", "%2A").
                                replace("%7E", "~");
                }
            }
        }
        restAuth.setQueryString(queryString);

        if (restAuth.verifySignature(request.getMethod(), cloudSecretKey, signature, sigMethod)) {
            UserContext.current().initContext(cloudAccessKey, cloudSecretKey, cloudAccessKey, "REST request", null);
            return true;
        } else
            throw new PermissionDeniedException("Invalid signature");
    }

    /**
     * We check this to reduce replay attacks.
     *
     * @param timeStamp
     * @return true - if the request is not longer valid, false otherwise
     * @throws ParseException
     */
    private boolean hasSignatureExpired(String timeStamp) {
        Calendar cal = EC2RestAuth.parseDateString(timeStamp);
        if (null == cal)
            return false;

        Date expiredTime = cal.getTime();
        Date today = new Date(); // -> gets set to time of creation
        if (0 >= expiredTime.compareTo(today)) {
            logger.debug("timestamp given: [" + timeStamp + "], now: [" + today.toString() + "]");
            return true;
        } else
            return false;
    }

}
