package helios.torana.client.server_authentication;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ServerAuthentication {

    private Authentication authentication;

    public ServerAuthentication(String userName, String password) {
        authentication = new Authentication(userName, password);
    }

    private class Authentication extends Authenticator {
        private final String userName;
        private final String password;

        Authentication(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password.toCharArray());
        }
    }

    public Authentication getAuthentication() {
        return authentication;
    }

}