package com.example.sally.parkingapp.item;

public class Parking {
    long id;
    private String area, name, address, serviceTime;
    private double lat, lon;

    public Parking(){}

    public Parking(String area, String name, String address, String serviceTime, double lat, double lon){
        this.area = area;
        this.name = name;
        this.address = address;
        this.serviceTime = serviceTime;
        this.lat = lat;
        this.lon = lon;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getArea() {
        return area;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setServiceTime(String serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getServiceTime() {
        return serviceTime;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public double getLat() {
        return lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public double getLon() {
        return lon;
    }
}
