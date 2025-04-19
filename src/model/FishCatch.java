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
public class FishCatch {
    private String id, boat, fishType, date, quantity;

    public FishCatch(String id, String boat, String fishType, String date, String quantity) {
        this.id = id;
        this.boat = boat;
        this.fishType = fishType;
        this.date = date;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getBoat() { return boat; }
    public String getFishType() { return fishType; }
    public String getDate() { return date; }
    public String getQuantity() { return quantity; }

    public void setBoat(String boat) { this.boat = boat; }
    public void setFishType(String fishType) { this.fishType = fishType; }
    public void setDate(String date) { this.date = date; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
