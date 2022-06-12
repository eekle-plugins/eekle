import arc.*
import arc.util.*
import mindustry.*
import mindustry.content.*
import mindustry.game.EventType.*
import mindustry.gen.*
import mindustry.mod.*

class MainPlugin() : Plugin() {
    init {
        Log.info("eekle loaded!")
    }

    private fun getPrefix(program: String): String {
        return "[blue]<" + program + ">[]: "
    }

    override fun registerClientCommands(handler: CommandHandler) {
        handler.register(
            "dm", "<player> <text...>", "DM another player"
        ) { args: Array<String>, player: Player ->
            val other = Groups.player.find {
                it.name.equals(args[0], true)
            }
            other?.sendMessage("[lightgray]<" + player.name.toString() + ">:[] " + args[1])
                ?: player.sendMessage(getPrefix("DM") + "[red]Player '" + args[0] + "' does not exist")
        }

        handler.register(
            "rtv", "[map] [vote]", "Switch to a different map"
        ) { args: Array<String>, player: Player -> 
            var map: String = "";
            if (args.size == 0) {
                val popularMap = RTV.popularMap
                if (popularMap != null) {
                    map = popularMap
                } else {
                    player.sendMessage(getPrefix("RTV") + "[red]No one has voted; specify a map.")
                    return@register
                }
            } else {
                map = args[0]
            }

            var vote: Boolean = true
            if (args.size > 1) {
                if (args[1].toLowerCase() == "n" || args[1].toLowerCase() == "no") {
                    vote = false
                } else if (args[1].toLowerCase() == "y" || args[1].toLowerCase() == "yes") {
                    vote = true
                } else {
                    player.sendMessage(getPrefix("RTV") +  "[red]Vote must be 'yes' or 'no'.")
                    return@register
                }
            }

            val realMap = RTV.setVote(player, map, vote)
            if (realMap == null) {
                player.sendMessage(getPrefix("RTV") + "[red]Map '" + map + "' does not exist.")
            } else {
                val votes = RTV.getVote(realMap)
                if (vote) {
                    Call.sendMessage(getPrefix("RTV") + "'" + player.name + "[]' has voted for '" + realMap + "' (" + votes + "/" + RTV.requiredVotes + ")")
                } else {
                    Call.sendMessage(getPrefix("RTV") + "'" + player.name + "[]' has withdrawn their vote for '" + realMap + "' ("  + votes + "/" + RTV.requiredVotes + ")")
                }
            }
            
            val newMap = RTV.checkAndUpdate()
            if (newMap != null) {
                Call.sendMessage(getPrefix("RTV") + "Vote passed. Changing the map to '" + newMap + "'")
            }
        }
    }

    override fun registerServerCommands(handler: CommandHandler) {
        // stuffz
    }
}

