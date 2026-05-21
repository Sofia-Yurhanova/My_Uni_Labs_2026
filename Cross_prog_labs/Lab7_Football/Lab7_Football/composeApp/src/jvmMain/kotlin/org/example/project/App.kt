package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// === 1. МОДЕЛІ ДАНИХ (JSON) ===

@Serializable
data class TeamsResponse(val teams: List<Team>? = null)

@Serializable
data class Team(
    val idTeam: String,
    val strTeam: String,
    val strLeague: String? = null,
    val strDescriptionEN: String? = null
)

@Serializable
data class PlayersResponse(val player: List<Player>? = null)

@Serializable
data class Player(
    val idPlayer: String,
    val strPlayer: String,
    val strTeam: String? = null,
    val strSport: String? = null,
    val strPosition: String? = null,
    val strDescriptionEN: String? = null
)

// === 2. REST-КЛІЄНТ (KTOR) ===

class SportsDbClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Ігноруємо зайві поля в JSON
                isLenient = true
            })
        }
    }

    private val baseUrl = "https://www.thesportsdb.com/api/v1/json/3"

    suspend fun getNFLTeams(): List<Team> {
        return try {
            val response: TeamsResponse = client.get("$baseUrl/search_all_teams.php?l=NFL").body()
            response.teams ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchPlayer(surname: String): Player? {
        return try {
            val response: PlayersResponse = client.get("$baseUrl/searchplayers.php?p=$surname").body()
            response.player?.forEach {
                println("Знайдено: ${it.strPlayer}, Спорт: ${it.strSport}")
            }
            // Шукаємо гравця саме з американського футболу
            response.player?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// === 3. ВІЗУАЛЬНИЙ ІНТЕРФЕЙС (COMPOSE) ===

@Composable
fun App() {
    val apiClient = remember { SportsDbClient() }
    val coroutineScope = rememberCoroutineScope()

    var teams by remember { mutableStateOf<List<Team>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchedPlayer by remember { mutableStateOf<Player?>(null) }
    var isPlayerSearched by remember { mutableStateOf(false) }

    // Завантаження списку команд під час запуску програми
    LaunchedEffect(Unit) {
        teams = apiClient.getNFLTeams()
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // ЛІВА ЧАСТИНА: Список команд
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Команди NFL", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(teams) { team ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(team.strTeam, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6)
                                Text("Ліга: ${team.strLeague ?: "Невідомо"}", color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = team.strDescriptionEN?.take(150)?.plus("...") ?: "Опис відсутній",
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ПРАВА ЧАСТИНА: Пошук гравця
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Пошук гравця", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Введіть прізвище (напр., Brady)") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                searchedPlayer = apiClient.searchPlayer(searchQuery)
                                isPlayerSearched = true
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Знайти")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Блок з результатом пошуку
                if (isPlayerSearched) {
                    if (searchedPlayer != null) {
                        Card(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xFFE3F2FD)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Гравець: ${searchedPlayer!!.strPlayer}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h6)
                                Text("Команда: ${searchedPlayer!!.strTeam ?: "Вільний агент"}")
                                Text("Позиція: ${searchedPlayer!!.strPosition ?: "Не вказано"}")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = searchedPlayer!!.strDescriptionEN?.take(300)?.plus("...") ?: "Опис відсутній",
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    } else {
                        Text("Гравця з таким прізвищем в Американському футболі не знайдено.", color = Color.Red)
                    }
                }
            }
        }
    }
}