package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import com.google.gson.Gson;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.place.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.YtPost;
import org.bson.Document;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Date;

public class User implements Serializable {

	public enum Role {USER,MODERATOR,ADMIN}

	private String _id;
	private String username;
	private String email;
	private String password;
	private String name;
	private String surname;
	private Date birthDate;
	private Role role;
	private Image profilePic;
	private List<YtPost> publishedPost;
	private List<User> followedUsers;
	private List<Place> favouritePlaces;
	private List<YtPost> likedPosts;
	private int totalPost;

	//need default constructor for JSON Parsing
	public User(){

	}

	public User(Document doc){
		this._id = doc.get("_id").toString();
		this.username = doc.get("username").toString();
		this.password = doc.get("password").toString();
		this.email = doc.get("email").toString();
		this.name = doc.get("name").toString();
		this.surname = doc.get("surname").toString();
		this.role = User.Role.valueOf(doc.get("role").toString());
		this.birthDate = (Date) doc.get("birthDate");
		this.profilePic = new Image();
		this.profilePic.setPath(doc.get("profilePic").toString());
	}

	public static User buildUser(@NotNull Document doc){
		Gson g = new Gson();
		User u = g.fromJson(doc.toJson(), User.class);
		return u;
	}

	public User(String _id, String nm, String snm, String uname, String psw, String email, Role role) {
		this._id = _id;
		this.name = nm;
		this.surname = snm;
		this.username = uname;
		this.password = psw;
		this.email = email;
		this.role = role;
	}


	public String getId() {
		return _id;
	}

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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	// write methods to retrive: pathProfilePic, followedUsers, favouritePosts, likedPosts, totalPosts

	@Override
	public String toString() {
		return "User{" +
				"id=" + _id +
				", name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", email='" + email + '\'' +
				", role='" + role + '\'' +
				'}';
	}
}
