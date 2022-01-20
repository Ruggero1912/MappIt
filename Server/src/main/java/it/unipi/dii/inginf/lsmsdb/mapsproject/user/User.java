package it.unipi.dii.inginf.lsmsdb.mapsproject.user;

import com.google.gson.Gson;
import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import it.unipi.dii.inginf.lsmsdb.mapsproject.model.Image;
import it.unipi.dii.inginf.lsmsdb.mapsproject.post.PostPreview;
import org.bson.Document;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class User implements Serializable {

	public static final String KEY_USER_COLLECTION = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "collectionName");
	public static final String KEY_ID = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "id");
	public static final String KEY_USERNAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "username");
	public static final String KEY_EMAIL = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "email");
	public static final String KEY_PASSWORD = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "password");
	public static final String KEY_NAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "name");
	public static final String KEY_SURNAME = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "surname");
	public static final String KEY_BIRTHDATE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "birthdate");
	public static final String KEY_ROLE = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "roles");
	public static final String KEY_PROFILE_PIC = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "profilepic");
	public static final String KEY_PUBLISHED_POSTS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "postsArray");
	public static final String KEY_FOLLOWERS = PropertyPicker.getCollectionPropertyKey(PropertyPicker.userCollection, "followers");

	public static final String NEO_USER_LABEL = PropertyPicker.getNodeLabel(PropertyPicker.userEntity);
	public static final String NEO_KEY_ID = PropertyPicker.getNodePropertyKey(PropertyPicker.userEntity, "id");
	public static final String NEO_KEY_USERNAME = PropertyPicker.getNodePropertyKey(PropertyPicker.userEntity, "username");

	public static final String NEO_RELATION_VISITED = PropertyPicker.getGraphRelationKey("visited");
	public static final String NEO_RELATION_FOLLOWS = PropertyPicker.getGraphRelationKey("follows");
	public static final String NEO_RELATION_FAVOURITES = PropertyPicker.getGraphRelationKey("favourites");
	public static final String NEO_RELATION_LIKES = PropertyPicker.getGraphRelationKey("likes");

	public enum Role {USER,MODERATOR,ADMIN}

	protected String _id;
	protected String username;
	protected String email;
	protected String password;
	protected String name;
	protected String surname;
	protected Date birthDate;
	protected List<String> roles;
	protected Image profilePic;
	protected int followersCounter;
	protected List<PostPreview> publishedPosts = new ArrayList<>();

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
		try {
			this.roles = doc.getList(KEY_ROLE, String.class);
		}catch (ClassCastException c){
			//this happens if it is parsing a document in which the role is a string instead of an array
			String role = doc.getString(KEY_ROLE);
			List<String> roles = new ArrayList<>();
			roles.add(role);
			this.roles = roles;
		}
		this.birthDate = (Date) doc.get(KEY_BIRTHDATE);
		this.profilePic = new Image(doc.getString(KEY_PROFILE_PIC));
		Object embeddedPosts = doc.get(KEY_PUBLISHED_POSTS);
		if(embeddedPosts instanceof ArrayList<?>) {
			ArrayList<?> embeddedPostsList = (ArrayList<?>) embeddedPosts;
			for (Object dboNestedObj : embeddedPostsList) {
				if (dboNestedObj instanceof Document) {
					this.publishedPosts.add(new PostPreview((Document) dboNestedObj));
				}
			}
		}
		this.followersCounter = doc.getInteger(KEY_FOLLOWERS, 0);
	}

	public static User buildUser(@NotNull Document doc){
		Gson g = new Gson();
		User u = g.fromJson(doc.toJson(), User.class);
		return u;
	}

	public User(String _id, String nm, String snm, String uname, String psw, String email, Date bday, List<String> roles, List<PostPreview> posts) {
		this._id = _id;
		this.name = nm;
		this.surname = snm;
		this.username = uname;
		this.password = psw;
		this.email = email;
		this.birthDate = bday;
		this.roles = roles;
		this.publishedPosts = posts;
	}

	public int getFollowersCounter(){
		return followersCounter;
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

	public String _getPassword() {
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

	public List<String> getUserRole() {
		return this.roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getProfilePic() {
		return profilePic.toString();
	}

	public Image profilePicImageObj() {
		return profilePic;
	}

	public void setProfilePic(Image pic) {
		this.profilePic = pic;
	}

	public List<PostPreview> getPublishedPosts(){ return this.publishedPosts; }


	@Override
	public String toString() {
		return "User{" +
				"id=" + _id +
				", name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", username='" + username + '\'' +
				", email='" + email + '\'' +
				", role='" + roles + '\'' +
				'}';
	}
}
