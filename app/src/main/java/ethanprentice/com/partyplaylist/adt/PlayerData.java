package ethanprentice.com.partyplaylist.adt;


import java.util.ArrayList;

// Used to sync data with devices that are not using the SpotifyPlayer
// This way devices that are not players do not stream in the background.
public class PlayerData {

    public String party;
    public PlayerStatus status;
    public int trackTime;
    public int currTrack;

    public ArrayList<Track> tracks;

    public PlayerData(String party, PlayerStatus status, int trackTime, int currTrack) {
        this.party = party;
        this.status = status;
        this.trackTime = trackTime;
        this.currTrack = currTrack;
    }

    public PlayerData() {

    }


    public void update(PlayerData data) {
        party = data.party;
        status = data.status;
        trackTime = data.trackTime;
        currTrack = data.currTrack;
    }

}
