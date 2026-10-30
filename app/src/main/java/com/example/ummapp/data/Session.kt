package com.example.ummapp.data

import android.content.Context
import androidx.core.content.edit

/** Remembers who is logged in between app launches. */
object Session {
    private const val PREFS = "session"
    private const val KEY_EMAIL = "email"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun currentUser(context: Context): User? =
        prefs(context).getString(KEY_EMAIL, null)?.let(MockData::findUser)

    fun login(context: Context, user: User) = prefs(context).edit { putString(KEY_EMAIL, user.email) }

    fun logout(context: Context) = prefs(context).edit { remove(KEY_EMAIL) }
}
