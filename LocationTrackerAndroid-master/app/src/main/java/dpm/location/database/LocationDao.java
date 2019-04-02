package dpm.location.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.google.android.gms.tasks.Task;

import java.util.List;

@Dao
public interface LocationDao {
    @Query("SELECT * FROM locationpoint")
    List<LocationPoint> getAll();

    @Insert
    void insert(LocationPoint locationPoint);

    @Delete
    void delete(LocationPoint locationPoint);

    @Update
    void update(LocationPoint locationPoint);
}
