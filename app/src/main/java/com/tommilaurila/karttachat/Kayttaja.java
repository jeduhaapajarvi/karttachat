package com.tommilaurila.karttachat;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by tommi.laurila on 26.3.2015.
 */
public class Kayttaja {
    private int kayttaja_id;
    private String nimimerkki;
    private String salasana;
    private int taso;
    private String perustamisaika;
    private String viimeksi_nahty;
    private int ryhma_id;
    private int tila;
    private Sijainti sijainti;
    private Marker merkki;
    private String serveriAika;

    public Kayttaja() {}

    public Kayttaja(String nimimerkki, String salasana) {
        this.nimimerkki = nimimerkki;
        this.salasana = salasana;
    }

    public int getKayttaja_id() {
        return kayttaja_id;
    }

    public void setKayttaja_id(int kayttaja_id) {
        this.kayttaja_id = kayttaja_id;
    }

    public String getNimimerkki() {
        return nimimerkki;
    }

    public void setNimimerkki(String nimimerkki) {
        this.nimimerkki = nimimerkki;
    }

    public String getSalasana() {
        return salasana;
    }

    public void setSalasana(String salasana) {
        this.salasana = salasana;
    }

    public int getTaso() {
        return taso;
    }

    public void setTaso(int taso) {
        this.taso = taso;
    }

    public String getPerustamisaika() {
        return perustamisaika;
    }

    public void setPerustamisaika(String perustamisaika) {
        this.perustamisaika = perustamisaika;
    }

    public String getViimeksi_nahty() {
        return viimeksi_nahty;
    }

    public void setViimeksi_nahty(String viimeksi_nahty) {
        this.viimeksi_nahty = viimeksi_nahty;
    }

    public int getRyhma_id() {
        return ryhma_id;
    }

    public void setRyhma_id(int ryhma_id) {
        this.ryhma_id = ryhma_id;
    }

    public int getTila() {
        return tila;
    }

    public void setTila(int tila) {
        this.tila = tila;
    }

    public Sijainti getSijainti() {
        return sijainti;
    }

    public void setSijainti(Sijainti sijainti) {
        this.sijainti = sijainti;
    }

    public Marker getMerkki() {
        return merkki;
    }

    public void setMerkki(Marker merkki) {
        this.merkki = merkki;
    }

    public String getServeriAika() { return serveriAika; }

    public void setServeriAika(String sa) { this.serveriAika = sa; }
}
