package models;

/**
 * Created by prapat on 11/27/2016 AD.
 */
public class JMessage {

    public String created;
    public String text;
    public String photoUrl;
    public String name;

    public JMessage(String created
            , String text
            , String photoUrl
            , String name) {
        this.created = created;
        this.text = text;
        this.photoUrl = photoUrl;
        this.name = name;
    }

}
