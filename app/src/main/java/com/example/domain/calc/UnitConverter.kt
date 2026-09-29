package com.example.domain.calc

import kotlin.math.roundToInt

object UnitConverter {

    fun kwToHp(kw: Double): Double = round2(kw / 0.7457)
    fun hpToKw(hp: Double): Double = round2(hp * 0.7457)

    fun wattsToKw(watts: Double): Double = round3(watts / 1000.0)
    fun kwToWatts(kw: Double): Double = round2(kw * 1000.0)

    fun ampsToMilliamps(a: Double): Double = round2(a * 1000.0)
    fun milliampsToAmps(ma: Double): Double = round4(ma / 1000.0)

    // AWG to mm2 lookup
    val awgTable = listOf(
        Pair("18 AWG", 0.82),
        Pair("16 AWG", 1.31),
        Pair("14 AWG", 2.08),
        Pair("12 AWG", 3.31),
        Pair("10 AWG", 5.26),
        Pair("8 AWG", 8.37),
        Pair("6 AWG", 13.3),
        Pair("4 AWG", 21.2),
        Pair("2 AWG", 33.6),
        Pair("1 AWG", 42.4),
        Pair("1/0 AWG", 53.5),
        Pair("2/0 AWG", 67.4),
        Pair("3/0 AWG", 85.0),
        Pair("4/0 AWG", 107.0)
    )

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
    private fun round3(value: Double): Double = (value * 1000.0).roundToInt() / 1000.0
    private fun round4(value: Double): Double = (value * 10000.0).roundToInt() / 10000.0
}
