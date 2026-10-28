package com.eyal.aimproxy

import android.content.Context

object FriendStore {
    private const val PREF = "aimproxy"
    private const val KEY = "friends"

    fun list(ctx: Context): MutableList<String> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY, "") ?: ""
        if (raw.isBlank()) return mutableListOf()
        return raw.split('\n').map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
    }

    fun save(ctx: Context, list: List<String>) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, list.joinToString("\n"))
            .apply()
    }

    fun add(ctx: Context, name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        val l = list(ctx)
        if (l.none { it.equals(n, true) }) {
            l.add(n)
            save(ctx, l)
        }
    }

    fun clear(ctx: Context) = save(ctx, emptyList())
}
