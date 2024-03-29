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

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jobayerjim9.satnav.data.model.SatEntry
import com.jobayerjim9.satnav.data.model.SatTrans

@Database(entities = [SatEntry::class, SatTrans::class], version = 3)
@TypeConverters(RoomConverters::class)
abstract class SatelliteDb : RoomDatabase() {

    abstract fun satelliteDao(): SatelliteDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE sources")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE trans_backup (uuid TEXT NOT NULL, info TEXT NOT NULL, isAlive INTEGER NOT NULL, downlink INTEGER, uplink INTEGER, mode TEXT, isInverted INTEGER NOT NULL, catNum INTEGER NOT NULL, PRIMARY KEY(uuid))")
        database.execSQL("INSERT INTO trans_backup (uuid, info, isAlive, downlink, uplink, mode, isInverted, catNum) SELECT uuid, description, isAlive, downlinkLow, uplinkLow, mode, isInverted, catNum FROM transmitters")
        database.execSQL("DROP TABLE transmitters")
        database.execSQL("ALTER TABLE trans_backup RENAME TO transmitters")
    }
}
