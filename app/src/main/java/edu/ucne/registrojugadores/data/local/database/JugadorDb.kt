package edu.ucne.registrojugadores.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.ucne.registrojugadores.data.local.dao.JugadorDao
import edu.ucne.registrojugadores.data.local.entities.JugadorEntity

@Database(
    entities = [
        JugadorEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JugadorDb : RoomDatabase() {
    abstract fun jugadorDao(): JugadorDao
}