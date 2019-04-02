package alarmmanager.com.alarmmanageroreosupportdemo.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {LocationPoint.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocationDao locationDao();
}
