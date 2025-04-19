/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author sange
 */
public class ComplianceNotice {
    private String id, boat, violation, violation_date, status;

    public ComplianceNotice(String id, String boat, String violation, String violation_date, String status) {
        this.id = id;
        this.boat = boat;
        this.violation = violation;
        this.violation_date = violation_date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBoat() {
        return boat;
    }

    public void setBoat(String boat) {
        this.boat = boat;
    }

    public String getViolation() {
        return violation;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public String getViolation_date() {
        return violation_date;
    }

    public void setViolation_date(String violation_date) {
        this.violation_date = violation_date;
    }

    

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}

