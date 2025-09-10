package edu.ucne.registrojugadores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import edu.ucne.registrojugadores.ui.theme.RegistroJugadoresTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var jugadorDb: JugadorDb

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        jugadorDb = Room.databaseBuilder(
            applicationContext,
            JugadorDb::class.java,
            "Jugadores.db"
        ).fallbackToDestructiveMigration()
            .build()

        setContent {
            RegistroJugadoresTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        JugadorScreen()
                    }
                }
            }
        }
    }

    @Composable
    fun JugadorListScreen(jugadorList: List<JugadorEntity>) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Lista de Jugadores")
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(jugadorList) {
                    JugadorRow(it)
                }
            }
        }
    }

    @Composable
    private fun JugadorRow(it: JugadorEntity) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(modifier = Modifier.weight(1f), text = it.jugadorId.toString())
            Text(
                modifier = Modifier.weight(2f),
                text = it.nombre,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(modifier = Modifier.weight(2f), text = it.partidas.toString())
        }
        HorizontalDivider()
    }

    @Entity(tableName = "Jugadores")
    data class JugadorEntity(
        @PrimaryKey
        val jugadorId: Int? = null,
        val nombre: String = "",
        val partidas: Int = 0
    )

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

    @Composable
    fun JugadorScreen(
    ) {
        var nombre: String by remember { mutableStateOf("") }
        var partidas: Int by remember { mutableStateOf(0) }
        var errorMessage: String? by remember { mutableStateOf(null) }

        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Registro de Jugadores")
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        OutlinedTextField(
                            label = { Text(text = "Nombre") },
                            value = nombre,
                            onValueChange = { nombre = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            label = { Text(text = "Partidas") },
                            value = partidas.toString(),
                            onValueChange = { partidas = it.toIntOrNull() ?: 0 },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        errorMessage?.let {
                            Text(text = it, color = Color.Red)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedButton(
                                onClick = {

                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "new button"
                                )
                                Text(text = "Nuevo")
                            }
                            val scope = rememberCoroutineScope()
                            OutlinedButton(
                                onClick = {
                                    if (nombre.isBlank())
                                        errorMessage = "Nombre vacio"

                                    scope.launch {
                                        saveJugador(
                                            JugadorEntity(
                                                nombre = nombre,
                                                partidas = partidas
                                            )
                                        )
                                        nombre = ""
                                        partidas = 0
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "save button"
                                )
                                Text(text = "Guardar")
                            }
                        }
                    }
                }
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                val jugadorList by jugadorDb.jugadorDao().getAll()
                    .collectAsStateWithLifecycle(
                        initialValue = emptyList(),
                        lifecycleOwner = lifecycleOwner,
                        minActiveState = Lifecycle.State.STARTED
                    )
                JugadorListScreen(jugadorList)
            }
        }
    }
    private suspend fun saveJugador(jugador: JugadorEntity) {
        jugadorDb.jugadorDao().save(jugador)
    }
}