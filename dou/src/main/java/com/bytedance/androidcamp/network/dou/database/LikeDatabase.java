package com.bytedance.androidcamp.network.dou.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LikeEntity.class}, version = 1, exportSchema = false)
public abstract class LikeDatabase extends RoomDatabase {
    private static volatile LikeDatabase INSTANCE;
    public abstract LikeDao LikeDao();

    public LikeDatabase() {

    }

    public static LikeDatabase inst(Context context) {
        if (INSTANCE == null) {
            synchronized (LikeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LikeDatabase.class, "like_list.db").build();
                }
            }
        }
        return INSTANCE;
    }

}
