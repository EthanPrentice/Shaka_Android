package ethanprentice.com.partyplaylist.adt;

/**
 * Created by Ethan on 2018-07-01.
 */

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;
import org.codehaus.jackson.map.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class User {

    @JsonProperty("access_token")   public String 		token;
    @JsonProperty("token_type")     public String 		tokenType;
    @JsonProperty("expires_at")     public Timestamp 	expiresAt;
    @JsonProperty("refresh_token")  public String 		refreshToken;
    @JsonProperty("display_name")   public String   	displayName;
    public String 		scope;
    public String 		username;
    public UserType 	type;
    public SpotifyImage[]  images;
    public boolean      owner;


    @JsonSetter("expires_in")
    public void setExpiry(Object obj) {
        if (obj instanceof Timestamp)
            expiresAt = (Timestamp) obj;
        else if (obj instanceof Integer)
            expiresAt = new Timestamp(new Date(new Date().getTime() + (int) obj * 1000).getTime());
        expiresAt.setNanos(0);
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            e.printStackTrace();
            return super.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            ObjectMapper om = new ObjectMapper();
            try {
                return om.writeValueAsString(this).equals(om.writeValueAsString(obj));
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return username.hashCode()/2 + displayName.hashCode()/2;
    }

    @JsonIgnore
    public String getShakaHeader() {
        return username + " : " + token;
    }

    @JsonIgnore
    public String getSpotifyHeader() {
        return "Authorization : " + tokenType + " " + token;
    }
}
