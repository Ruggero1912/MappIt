package it.unipi.dii.inginf.lsmsdb.mapsproject.user;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;

import java.util.Date;
import org.bson.Document;

public class RegistrationUser {

    private String username;
    private String email;
    private String password;
    private String name;
    private String surname;
    private Date birthDate;
    private String countryCode;
    private String profilePic;

    public String getUsername() {
        return username;
    }

    public void setUsername(String uname) {
        this.username = uname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String psw) {
        this.password = psw;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String sname) {
        this.surname = sname;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountryCode(){ return this.countryCode; }

    public String getProfilePic(){
        return this.profilePic;
    }

    public Document createDocument(){
        String passwordHash = UserService.passwordEncryption(password);

        Document userDoc = new Document(User.KEY_USERNAME,username)
                .append(User.KEY_PASSWORD, passwordHash)
                .append(User.KEY_NAME, name)
                .append(User.KEY_SURNAME, surname)
                .append(User.KEY_EMAIL, email)
                .append(User.KEY_BIRTHDATE, birthDate)
                .append(User.KEY_ROLE, User.Role.USER.toString())
                .append(User.KEY_COUNTRY_CODE, countryCode)
                .append(User.KEY_PROFILE_PIC, profilePic);

        return userDoc;
    }
}
