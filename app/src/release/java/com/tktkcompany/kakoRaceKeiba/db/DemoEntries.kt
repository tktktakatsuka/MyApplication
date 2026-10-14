package com.tktkcompany.kakoRaceKeiba.db

import io.github.jan.supabase.SupabaseClient

/** リリースビルドでは仮の出馬表を作らない（デバッグ版は src/debug にある） */
internal object DemoEntries {
    @Suppress("UNUSED_PARAMETER")
    suspend fun build(client: SupabaseClient, fromDate: String): List<SupabaseManager.RaceEntry> = emptyList()
}
