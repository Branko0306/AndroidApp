package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import models.Sadrzaj;

@Dao
public interface SadrzajLogEntityDao {

    @Query("SELECT * FROM SadrzajLogEntity")
    List<SadrzajLogEntity> getAll();

    @Query("SELECT * FROM SadrzajLogEntity WHERE sadrzaj_pk = :sadrzaj_pk")
    SadrzajLogEntity getByID(int sadrzaj_pk);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSadrzajLogEntity(SadrzajLogEntity... entities);

    @Delete
    void delete(SadrzajLogEntity entity);

    @Update
    public void updateSadrzajLogEntity(SadrzajLogEntity... entities);
}
