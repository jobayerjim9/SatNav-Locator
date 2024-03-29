/*
 * Look4Sat. Amateur radio satellite tracker and pass predictor.
 * Copyright (C) 2019-2021 Arty Bishop (bishop.arty@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.jobayerjim9.satnav.data.database

import androidx.room.*
import com.github.amsacode.predict4java.Satellite
import com.jobayerjim9.satnav.data.model.SatEntry
import com.jobayerjim9.satnav.data.model.SatItem
import com.jobayerjim9.satnav.data.model.SatTrans
import kotlinx.coroutines.flow.Flow

@Dao
interface SatelliteDao {

    @Transaction
    @Query("SELECT catNum, name, isSelected FROM entries ORDER BY name ASC")
    fun getSatItems(): Flow<List<SatItem>>

    @Query("SELECT * FROM transmitters WHERE catNum = :catNum")
    fun getSatTransmitters(catNum: Int): Flow<List<SatTrans>>

    @Query("SELECT tle FROM entries WHERE isSelected = 1")
    suspend fun getSelectedSatellites(): List<Satellite>

    @Query("SELECT catNum FROM entries WHERE isSelected = 1")
    suspend fun getSelectedCatNums(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<SatEntry>)

    @Transaction
    suspend fun updateTransmitters(transmitters: List<SatTrans>) {
        deleteTransmitters()
        insertTransmitters(transmitters)
    }

    @Query("DELETE from transmitters")
    suspend fun deleteTransmitters()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransmitters(transmitters: List<SatTrans>)

    @Transaction
    suspend fun restoreEntriesSelection(catNums: List<Int>, isSelected: Boolean) {
        clearEntriesSelection()
        catNums.forEach { catNum -> updateEntrySelection(catNum, isSelected) }
    }

    @Transaction
    suspend fun updateEntriesSelection(catNums: List<Int>, isSelected: Boolean) {
        catNums.forEach { catNum -> updateEntrySelection(catNum, isSelected) }
    }

    @Query("UPDATE entries SET isSelected = 0")
    suspend fun clearEntriesSelection()

    @Query("UPDATE entries SET isSelected = :isSelected WHERE catNum = :catNum")
    suspend fun updateEntrySelection(catNum: Int, isSelected: Boolean)
}
