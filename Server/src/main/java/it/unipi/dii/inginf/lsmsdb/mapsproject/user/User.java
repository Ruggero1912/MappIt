package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import com.google.gson.Gson;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import org.bson.Document;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Date;

public class User implements Serializable {

	public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "id");
	public static final String KEY_USERNAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "username");
	public static final String KEY_EMAIL = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "email");
	public static final String KEY_PASSWORD = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "password");
	public static final String KEY_NAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "name");
	public static final String KEY_SURNAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "surname");
	public static final String KEY_BIRTHDATE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "birthdate");
	public static final String KEY_ROLE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "role");
	public static final String KEY_PROFILE_PIC = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "profilepic");
	public static final String KEY_PUBLISHED_POSTS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "postsArray");

	public static final String NEO_KEY_ID = PropertyPicker.getNodePropertyKey(PropertyPicker.neoUserNode, "id");


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
	private List<String> publishedPostsId;
	private List<String> followedUsersId;
	private List<String> favouritePlacesId;
	private List<String> likedPostsId;
	private int totalPost;

	//need default constructor for JSON Parsing
	public User(){

	}

	public User(Document doc){
		this._id = doc.get(KEY_ID).toString();
		this.username = doc.get(KEY_USERNAME).toString();
		this.password = doc.get(KEY_PASSWORD).toString();
		this.email = doc.get(KEY_EMAIL).toString();
		this.name = doc.get(KEY_NAME).toString();
		this.surname = doc.get(KEY_SURNAME).toString();
		this.role = User.Role.valueOf(doc.get(KEY_ROLE).toString());
		this.birthDate = (Date) doc.get(KEY_BIRTHDATE);
		this.profilePic = new Image();
		this.profilePic.setPath(doc.get(KEY_PROFILE_PIC).toString());
		this.publishedPostsId = (List<String>) doc.get(KEY_PUBLISHED_POSTS, List.class);
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
