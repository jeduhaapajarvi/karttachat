package com.tommilaurila.karttachat;

/**
 * Created by tommi.laurila on 28.4.2015.
 */
public class Sijainti {

    private int sijainti_id;
    private double lat;
    private double lng;
    private String aikaleima;
    private int kayttaja_id;

    public Sijainti() {}

    public Sijainti(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public int getSijainti_id() {
        return sijainti_id;
    }

    public void setSijainti_id(int sijainti_id) {
        this.sijainti_id = sijainti_id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAikaleima() {
        return aikaleima;
    }

    public void setAikaleima(String aikaleima) {
        this.aikaleima = aikaleima;
    }

    public int getKayttaja_id() {
        return kayttaja_id;
    }

    public void setKayttaja_id(int kayttaja_id) {
        this.kayttaja_id = kayttaja_id;
    }
}
