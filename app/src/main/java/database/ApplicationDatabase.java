package database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = { SadrzajLogEntity.class }, version = 1, exportSchema = false)
public abstract class ApplicationDatabase extends RoomDatabase {
    public abstract SadrzajLogEntityDao SadrzajLogEntityDao();
}


