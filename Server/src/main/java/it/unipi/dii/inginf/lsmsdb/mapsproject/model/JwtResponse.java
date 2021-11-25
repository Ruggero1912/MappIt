package it.unipi.dii.inginf.lsmsdb.mapsproject.model;

import java.io.Serializable;
import java.util.Date;

public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;
    private final String jwttoken;
    private final Date expires;

    public JwtResponse(String jwttoken, Date exp) {
        this.jwttoken = jwttoken;
        this.expires = exp;
    }

    public String getToken() {
        return this.jwttoken;
    }
}