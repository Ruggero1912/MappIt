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

    public Document createDocument(){
        String passwordHash = UserService.passwordEncryption(password);
        Image img = new Image(); // add a default profile pic for the new user

        Document userDoc = new Document("username",username)
                .append("password", passwordHash)
                .append("name", name)
                .append("surname", surname)
                .append("email", email)
                .append("birthDate", birthDate)
                .append("role", User.Role.USER.toString())
                .append("profilePic", img.getPath());

        return userDoc;
    }
}
