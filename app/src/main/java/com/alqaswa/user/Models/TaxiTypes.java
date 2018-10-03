package com.alqaswa.user.Models;

/**
 * Created by user on 1/9/2017.
 */

public class TaxiTypes {
    private String id;
    private String taxitype;
    private String taxiimage;
    private String taxi_cost;
    private String taxi_seats;
    private String taxi_price_min,currencey_unit;
    private String taxi_price_distance,basefare;

    public String getTaxitype() {
        return taxitype;
    }

    public void setTaxitype(String taxitype) {
        this.taxitype = taxitype;
    }

    public String getTaxiimage() {
        return taxiimage;
    }

    public void setTaxiimage(String taxiimage) {
        this.taxiimage = taxiimage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaxi_cost() {
        return taxi_cost;
    }

    public void setTaxi_cost(String taxi_cost) {
        this.taxi_cost = taxi_cost;
    }

    public String getTaxi_seats() {
        return taxi_seats;
    }

    public void setTaxi_seats(String taxi_seats) {
        this.taxi_seats = taxi_seats;
    }

    public String getTaxi_price_min() {
        return taxi_price_min;
    }

    public void setTaxi_price_min(String taxi_price_min) {
        this.taxi_price_min = taxi_price_min;
    }

    public String getTaxi_price_distance() {
        return taxi_price_distance;
    }

    public void setTaxi_price_distance(String taxi_price_distance) {
        this.taxi_price_distance = taxi_price_distance;
    }

    public String getBasefare() {
        return basefare;
    }

    public void setBasefare(String basefare) {
        this.basefare = basefare;
    }

    public String getCurrencey_unit() {
        return currencey_unit;
    }

    public void setCurrencey_unit(String currencey_unit) {
        this.currencey_unit = currencey_unit;
    }
}
