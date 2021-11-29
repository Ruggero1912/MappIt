package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Place;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Post;

import java.io.Serializable;
import java.util.List;
import java.util.Date;

public class User implements Serializable {

	public enum Role {USER,MODERATOR,ADMIN}

	private int id;
	private String username;
	private String email;
	private String password;
	private String name;
	private String surname;
	private Date birthDate;
	private Role role;
	private Image profilePic;
	private List<Post> publishedPost;
	private List<User> followedUsers;
	private List<Place> favouritePlaces;
	private List<Post> likedPosts;
	private int totalPost;

	//need default constructor for JSON Parsing
	public User(){

	}


	public User(int id, String nm, String snm, String uname, String psw, String email, Role role) {
		this.id = id;
		this.name = nm;
		this.surname = snm;
		this.username = uname;
		this.password = psw;
		this.email = email;
		this.role = role;
	}

	//used in login auth
	public User(String uname, String psw){
		this.username = uname;
		this.password = psw;
		this.id = -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
				"id=" + id +
				", name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", email='" + email + '\'' +
				", role='" + role + '\'' +
				'}';
	}
}
