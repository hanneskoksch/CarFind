package de.hdmstuttgart.carfind;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Car {

    @PrimaryKey(autoGenerate = true)
    public int uid;
    public String carModel, licencePlate, level, spot, annotation, filepath, longitude, latitude;

    public Car(String carModel, String licencePlate, String level, String spot, String annotation, String longitude, String latitude, String filepath) {
        this.carModel = carModel;
        this.licencePlate = licencePlate;
        this.level = level;
        this.spot = spot;
        this.annotation = annotation;
        this.longitude = longitude;
        this.latitude = latitude;
        this.filepath = filepath;
    }
}
