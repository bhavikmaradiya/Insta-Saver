package sample.InstaModals;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User {


    @SerializedName("full_name")
    @Expose
    private String fullName;

    @SerializedName("profile_pic_url_hd")
    @Expose
    private String profilePicUrlHd;

    @SerializedName("username")
    @Expose
    private String username;


    public String getFullName() {
        return fullName;
    }


    public String getProfilePicUrlHd() {
        return profilePicUrlHd;
    }


    public String getUsername() {
        return username;
    }


}
