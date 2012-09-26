package io.cinderella.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SignatureException;
import java.text.ParseException;

/**
 * @author shane
 * @since 9/26/12
 */
public interface AuthenticationService {

    boolean authenticateRequest(HttpServletRequest request,
                                HttpServletResponse response) throws SignatureException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException;
}
