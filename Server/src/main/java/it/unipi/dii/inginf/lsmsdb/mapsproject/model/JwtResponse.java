package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import java.io.Serializable;
import java.util.Date;

public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final Date expirationDate;
    private final String userId;

    public JwtResponse(String jwttoken, Date exp, String uid) {
        this.jwttoken = jwttoken;
        this.expirationDate = exp;
        this.userId = uid;
    }

    public String getToken() {
        return this.jwttoken;
    }
    public Date getExpires() { return this.expirationDate; }
    public String getUserId() { return this.userId; }
}