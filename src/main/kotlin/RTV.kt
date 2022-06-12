import kotlin.collections.HashMap
import kotlin.collections.HashSet

import arc.Core
import arc.ApplicationListener
import arc.util.Reflect
import arc.Events

import mindustry.gen.Player
import mindustry.Vars
import mindustry.maps.Map
import mindustry.server.ServerControl
import mindustry.game.Team
import mindustry.game.EventType
import mindustry.gen.Groups

/** Methods relating to the RTV command */
object RTV {
    private val votes: HashMap<String, HashSet<String>> = HashMap()

    val requiredVotes: Int
        get() {
            return Math.ceil((Groups.player.size() + 1) / 2.0) as Int
        }

    fun checkAndUpdate(): String? {
        // filter unused IPs (players that left)
     
        // check
        for ((mapName, voteList) in votes) {
            if (voteList.size >= requiredVotes) {
                // find map with that name
                for (map in Vars.maps.all()) {
                    if (map.name() == mapName) {
                        changeMap(map)
                        return mapName
                    }
                }
            }
        }

        return null
    }

    private fun changeMap(map: Map) {
        for (it: ApplicationListener in Core.app.getListeners()) {
            if (it is ServerControl) {
                Reflect.set(it, "nextMapOverride", map)
                Events.fire(EventType.GameOverEvent(Team.crux))
                return
            }
        }
    }

    val popularMap: String?
        get() {
            var maxSize: Int = 0
            var map: String? = null
            for ((mapName, voteList) in votes) {
                if (voteList.size > maxSize) {
                    map = mapName
                    maxSize = voteList.size
                }
            }
            return map
        }

    fun getVote(query: String): Int {
        val map = findMap(query)?.name()
        if (map == null) {
            return 0
        }

        val votes = votes.get(map)?.size
        if (votes != null) {
            return votes
        } else {
            return 0
        }
    }

    fun setVote(player: Player, map: String, vote: Boolean): String? {
        val mapObj = findMap(map)
        if (mapObj != null) {
            val votelist = votes.getOrElse(mapObj.name(), { -> HashSet() })
            votes.put(mapObj.name(), votelist)
            if (vote) {
                votelist.add(player.ip())
            } else {
                votelist.remove(player.ip())
            }
            return mapObj.name()
        } else {
            return null
        }
    }

    fun findMap(query: String): Map? {
        for (map: Map in Vars.maps.all()) {
            if (map.name().replace(" ", "").toLowerCase().contains(query.replace(" ", "").toLowerCase())) {
                return map
            }
        }

        return null
    }
}