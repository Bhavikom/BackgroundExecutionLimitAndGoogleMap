package alarmmanager.com.alarmmanageroreosupportdemo.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface OfferDAO {
    @Query("SELECT * FROM Hero")
    public List<Hero> getAllRecords();

    @Insert
    public void insertLatestOffer(Hero offer);

    @Query("DELETE FROM Hero")
    public void deleteOffers();
}
