package dpm.location.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.location.Location;

import com.google.android.gms.tasks.Task;

@Database(entities = {LocationPoint.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();
}
