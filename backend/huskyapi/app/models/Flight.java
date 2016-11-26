package models;

import java.io.Serializable;

public class Flight implements Serializable {

    public String id;
    public String name;
    public String airline;
    public String arrival;
    public String arrivalDate;
    public String arrivalTime;
    public String departure;
    public String departureDate;
    public String departureTime;
    public String photo;

    public Flight(String id
            , String name
            , String airline
            , String arrival
            , String arrivalDate
            , String arrivalTime
            , String departure
            , String departureDate
            , String departureTime
            , String photo) {
        this.id = id;
        this.name = name;
        this.airline = airline;
        this.arrival = arrival;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.departure = departure;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.photo = photo;
    }
}
