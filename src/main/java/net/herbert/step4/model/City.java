package net.herbert.step4.model;

public class City {
    private String name;
    private String country;
    private String subcountry;
    private Integer geonameid;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getSubcountry() {
        return subcountry;
    }
    public void setSubcountry(String subcountry) {
        this.subcountry = subcountry;
    }
    public Integer getGeonameid() {
        return geonameid;
    }
    public void setGeonameid(Integer geonameid) {
        this.geonameid = geonameid;
    }
}
