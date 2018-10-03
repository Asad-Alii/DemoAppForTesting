package com.alqaswa.user.Models;

/**
 * Created by user on 2/3/2017.
 */

public class Later {
    private String req_id,req_date,req_type,req_pic,s_lat,s_lan,d_lat,d_lan,s_address,d_address;

    public String getReq_id() {
        return req_id;
    }

    public void setReq_id(String req_id) {
        this.req_id = req_id;
    }

    public String getReq_date() {
        return req_date;
    }

    public void setReq_date(String req_date) {
        this.req_date = req_date;
    }

    public String getReq_type() {
        return req_type;
    }

    public void setReq_type(String req_type) {
        this.req_type = req_type;
    }

    public String getReq_pic() {
        return req_pic;
    }

    public void setReq_pic(String req_pic) {
        this.req_pic = req_pic;
    }

    public String getS_address() {
        return s_address;
    }

    public void setS_address(String s_address) {
        this.s_address = s_address;
    }

    public String getD_address() {
        return d_address;
    }

    public void setD_address(String d_address) {
        this.d_address = d_address;
    }
}
