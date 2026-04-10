package com.antogian.shelvie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shelvie.security")
public class SecurityProperties {

    private String mode = "none";
    private Basic basic = new Basic();
    private Jwt jwt = new Jwt();

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Basic getBasic() { return basic; }
    public void setBasic(Basic basic) { this.basic = basic; }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public static class Basic {
        private String username = "admin";
        private String password = "changeme";

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Jwt {
        private String secret = "";
        private long expirationMs = 86400000L;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }
}