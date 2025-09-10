package edu.ucne.registrojugadores.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import edu.ucne.registrojugadores.data.local.entities.JugadorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JugadorDao {
    @Upsert()
    suspend fun save(jugador: JugadorEntity)
    @Query(
        """
        SELECT * 
        FROM Jugadores 
        WHERE jugadorId=:id  
        LIMIT 1
        """
    )
    suspend fun find(id: Int): JugadorEntity?

    @Delete
    suspend fun delete(jugador: JugadorEntity)

    @Query("SELECT * FROM Jugadores")
    fun getAll(): Flow<List<JugadorEntity>>
}