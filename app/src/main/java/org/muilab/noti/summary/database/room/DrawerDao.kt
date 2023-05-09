package org.muilab.noti.summary.database.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.muilab.noti.summary.model.NotiUnit

@Dao
interface DrawerDao {

    @Query("SELECT * FROM noti_drawer")
    fun getAll(): List<NotiUnit>

    @Query("SELECT * FROM noti_drawer WHERE pkgName = :pkgName AND sbnKey = :sbnKey")
    fun getBySbnKey(pkgName: String, sbnKey: String): List<NotiUnit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notiUnit: NotiUnit)

    @Query("DELETE FROM noti_drawer WHERE pkgName = :pkgName AND `when` = :when AND title = :title AND content = :content")
    fun deleteByVisibleAttr(pkgName: String, `when`: Long, title: String, content: String)

    @Query("DELETE FROM noti_drawer WHERE pkgName = :pkgName AND sbnKey = :sbnKey AND groupKey = :groupKey AND sortKey = :sortKey")
    fun deleteByPackageSortKey(pkgName: String, sbnKey: String, groupKey: String, sortKey: String)

    @Query("DELETE FROM noti_drawer WHERE pkgName = :pkgName AND groupKey = :groupKey")
    fun deleteByPackageGroup(pkgName: String, groupKey: String)

    @Query("DELETE FROM noti_drawer")
    fun deleteAll()
}