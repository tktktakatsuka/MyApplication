package com.tktkcompany.kakoRaceKeiba.db

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * デバッグビルド専用。出馬表(race_entries)がまだ無いとき、race_results に載っている実在の馬で
 * 次の土日の仮の出馬表を作る（画面の確認用。リリースビルドには入らない）。
 */
internal object DemoEntries {

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private const val FIELD_SIZE = 16

    @Volatile
    private var cache: List<SupabaseManager.RaceEntry>? = null

    suspend fun build(client: SupabaseClient, fromDate: String): List<SupabaseManager.RaceEntry> {
        cache?.let { return it }

        val rows = client.from("race_results").select(
            Columns.list("kaisaibi", "kaisaijo", "race_no", "race_title", "horse_name", "jockey", "weight", "hassou_time")
        ) {
            order("kaisaibi", Order.DESCENDING)
            limit(3000)
        }.decodeList<SupabaseManager.RaceResult>()
        if (rows.isEmpty()) return emptyList()

        // 直近の開催週に使われた競馬場を、今週も開催しているものとみなす
        val latest = LocalDate.parse(rows.first().kaisaibi, DATE_FORMAT)
        val courses = rows.filter { LocalDate.parse(it.kaisaibi, DATE_FORMAT) >= latest.minusDays(2) }
            .map { it.kaisaijo }.distinct()

        val start = LocalDate.parse(fromDate, DATE_FORMAT)
        val saturday = generateSequence(start) { it.plusDays(1) }.first { it.dayOfWeek == DayOfWeek.SATURDAY }
        val days = listOf(saturday, saturday.plusDays(1)).map { it.format(DATE_FORMAT) }

        val entries = mutableListOf<SupabaseManager.RaceEntry>()
        for (day in days) {
            for (course in courses) {
                val atCourse = rows.filter { it.kaisaijo == course && !it.horseName.isNullOrBlank() }
                // 馬ごとに一番新しい出走（騎手・斤量の引き継ぎ用）
                val pool = atCourse.distinctBy { it.horseName }
                for (raceNo in 9..12) {
                    val template = atCourse.firstOrNull { it.raceNo == raceNo } ?: continue
                    val random = Random("$day$course$raceNo".hashCode())
                    val horses = pool.shuffled(random).take(FIELD_SIZE)
                    horses.forEachIndexed { i, horse ->
                        entries += SupabaseManager.RaceEntry(
                            kaisaibi = day,
                            kaisaijo = course,
                            raceNo = raceNo,
                            raceTitle = template.raceTitle?.trim(),
                            raceInfo = template.hassouTime,
                            waku = waku(i + 1, horses.size).toString(),
                            horseNumber = (i + 1).toString(),
                            horseName = horse.horseName!!,
                            jockey = horse.jockey,
                            weight = horse.weight
                        )
                    }
                }
            }
        }
        cache = entries
        return entries
    }

    /** 頭数に応じた枠番（8頭を超えた分は外枠から2頭ずつにする） */
    private fun waku(number: Int, fieldSize: Int): Int {
        if (fieldSize <= 8) return number
        val base = fieldSize / 8
        val extra = fieldSize % 8
        var upTo = 0
        for (w in 1..8) {
            upTo += base + if (w > 8 - extra) 1 else 0
            if (number <= upTo) return w
        }
        return 8
    }
}
