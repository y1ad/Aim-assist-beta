package com.eyal.aimproxy

import android.content.Context

object Prefs {
    private const val P = "aimproxy_prefs"

    fun get(ctx: Context, key: String, def: String) =
        ctx.getSharedPreferences(P, Context.MODE_PRIVATE).getString(key, def) ?: def

    fun set(ctx: Context, key: String, value: String) {
        ctx.getSharedPreferences(P, Context.MODE_PRIVATE).edit().putString(key, value).apply()
    }

    fun proxyHost(ctx: Context) = get(ctx, "proxy_host", "fi11.bot-hosting.cloud")
    fun proxyPort(ctx: Context) = get(ctx, "proxy_port", "26283")
    fun remoteHost(ctx: Context) = get(ctx, "remote_host", "mco.cubecraft.net")
    fun remotePort(ctx: Context) = get(ctx, "remote_port", "19132")

    fun setProxy(ctx: Context, host: String, port: String) {
        set(ctx, "proxy_host", host.trim())
        set(ctx, "proxy_port", port.trim())
    }

    fun setRemote(ctx: Context, host: String, port: String) {
        set(ctx, "remote_host", host.trim())
        set(ctx, "remote_port", port.trim().ifEmpty { "19132" })
    }
}

data class ServerPreset(val name: String, val host: String, val port: String)

object ServerPresets {
    val all = listOf(
        ServerPreset("CubeCraft", "mco.cubecraft.net", "19132"),
        ServerPreset("The Hive", "geo.hivebedrock.network", "19132"),
        ServerPreset("Lifeboat", "play.lbsg.net", "19132"),
        ServerPreset("NetherGames", "play.nethergames.org", "19132"),
        ServerPreset("Galaxite", "play.galaxite.net", "19132"),
        ServerPreset("Mineville", "play.inpvp.net", "19132"),
        ServerPreset("Custom", "", "19132"),
    )
}
