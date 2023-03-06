package edu.mui.noti.noti.database.room

import androidx.room.*
import edu.mui.noti.noti.model.CurrentDrawer

@Dao
interface CurrentDrawerDao {
    @Query("SELECT notificationId FROM current_drawer_table")
    fun getAll(): List<String>

    @Query("SELECT DISTINCT packageName FROM current_drawer_table")
    fun getAllPackages(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currentDrawer: CurrentDrawer)

    @Query("DELETE FROM current_drawer_table WHERE notificationId = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM current_drawer_table WHERE packageName = :pkgName AND groupKey = :group AND sortKey = :sortKey")
    fun deleteByPackageSortKey(pkgName: String, group: String, sortKey: String)

    @Query("DELETE FROM current_drawer_table WHERE packageName = :pkgName AND groupKey = :group")
    fun deleteByPackageGroup(pkgName: String, group: String)

    @Query("SELECT * FROM current_drawer_table WHERE packageName = :pkgName AND groupKey = :group")
    fun getByPackageGroup(pkgName:String, group: String): List<CurrentDrawer>

    @Query("DELETE FROM current_drawer_table")
    fun deleteAll()
}