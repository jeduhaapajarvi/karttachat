package com.tommilaurila.karttachat;

/**
 * Created by tommi.laurila on 19.5.2015.
 */
public class Ryhma {
    private int ryhma_id;
    private int luoja; // ryhmän luojan id
    private String nimi;
    private String salasana;
    private String perustamisaika;

    // konstruktori
    public Ryhma() {}

    // get- ja set-metodit
    public int getRyhma_id() {
        return ryhma_id;
    }

    public void setRyhma_id(int ryhma_id) {
        this.ryhma_id = ryhma_id;
    }

    public int getLuoja() {
        return luoja;
    }

    public void setLuoja(int luoja) {
        this.luoja = luoja;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getSalasana() {
        return salasana;
    }

    public void setSalasana(String salasana) {
        this.salasana = salasana;
    }

    public String getPerustamisaika() {
        return perustamisaika;
    }

    public void setPerustamisaika(String perustamisaika) {
        this.perustamisaika = perustamisaika;
    }

    // ylikirjoitetaan toString-metodi siten, että se
    // palauttaa ryhmän nimen
    @Override
    public String toString() {
        return this.nimi;
    }
}
