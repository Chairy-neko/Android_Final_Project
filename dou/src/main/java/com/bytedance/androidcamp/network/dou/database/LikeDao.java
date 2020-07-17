package com.bytedance.androidcamp.network.dou.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * @author wangrui.sh
 * @since Jul 11, 2020
 */
@Dao
public interface LikeDao {
    @Query("SELECT * FROM like")
    List<LikeEntity> loadAll();

    @Query("SELECT * FROM like WHERE content IN (:userIds)")
    List<LikeEntity> loadAllByIds(String[] userIds);

    @Insert
    long addTodo(LikeEntity entity);

    @Update
    void updateTodo(LikeEntity entity);

    @Delete
    void delTodo(LikeEntity entity);

    @Query("DELETE FROM like")
    void deleteAll();

}
