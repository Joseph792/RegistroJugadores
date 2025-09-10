package edu.ucne.registrojugadores.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Jugadores")
data class JugadorEntity(
    @PrimaryKey
    val jugadorId: Int? = null,
    val nombre: String = "",
    val partidas: Int = 0
)