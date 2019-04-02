package alarmmanager.com.alarmmanageroreosupportdemo.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Hero.class}, version = 1)
public abstract class OfferDatabase extends RoomDatabase {
    public abstract OfferDAO offerDAO();
}

