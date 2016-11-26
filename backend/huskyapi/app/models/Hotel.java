package models;

import java.io.Serializable;

public class Hotel implements Serializable {

    public String id;
    public String name;
    public String link;
    public String photo;
    public Double price;
    public Integer score;

    public Hotel(String id
            , String name
            , String link
            , String photo
            , Double price
            , Integer score) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.photo = photo;
        this.price = price;
        this.score = score;
    }
}
