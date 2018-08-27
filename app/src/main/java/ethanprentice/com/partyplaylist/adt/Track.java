package ethanprentice.com.partyplaylist.adt;

/**
 * Created by Ethan on 2018-06-14.
 */

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.spotify.sdk.android.player.Metadata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSetter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ethanprentice.com.partyplaylist.R;

//Ignores all undeclared properties returned by the API
@JsonIgnoreProperties(ignoreUnknown=true)
public class Track {

    public static final byte PX64 = 2;
    public static final byte PX300 = 1;
    public static final byte PX640 = 0;

    public String username;
    public SpotifyImage[] albumImages;

    public String artistName;
    public String artistURI;

    public String albumName;
    public String albumURI;

    public String name;
    public String uri;

    @JsonSetter("album")
    public void setAlbum(NameURI album) {
        albumName = album.name;
        albumURI = album.uri;
        albumImages = album.images;
    }

    @JsonSetter("artists")
    public void setArtist(NameURI[] artists) {
        artistName = artists[0].name;
        artistURI = artists[0].uri;
    }

    @Override
    public String toString() {
        return name + albumName + " : " + artistName + "(" + uri + ") : ";
    }

    public Drawable getAlbumArtSmall(File cacheDir, Resources res) {
        try {
            // TODO: Read files from cacheDir if they exist
            return downloadAlbumArt(albumImages[PX64].url, cacheDir, res);
        } catch (Exception e) {
            e.printStackTrace();
            return new ColorDrawable(res.getColor(R.color.colorPrimaryDark, null));
        }
    }

    public Drawable getAlbumArtLarge(File cacheDir, Resources res) {
        try {
            // TODO: Read files from cacheDir if they exist
            return downloadAlbumArt(albumImages[PX640].url, cacheDir, res);
        } catch (Exception e) {
            e.printStackTrace();
            return new ColorDrawable(res.getColor(R.color.colorPrimaryDark, null));
        }
    }

    private Drawable downloadAlbumArt(String url, File cacheDir, Resources res) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();

        InputStream input = connection.getInputStream();
        Bitmap bmp = BitmapFactory.decodeStream(input);

        File file = new File(cacheDir, albumURI + "_icon");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BitmapDrawable(res, bmp);
    }

}

@JsonIgnoreProperties(ignoreUnknown=true)
class NameURI {
    public String name;
    public String uri;
    public SpotifyImage[] images;
}