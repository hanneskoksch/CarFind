package de.hdmstuttgart.carfind;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CarDao {

    @Query("SELECT * FROM car")
    List<Car> getAll();

    @Query("SELECT * FROM car WHERE uid = :uid")
    Car getPrimaryKey(int uid);

    @Insert
    void insert(Car car);

    @Delete
    void delete(Car car);

    @Update
    void update(Car car);

    @Query("DELETE FROM car")
    void delete();

}
