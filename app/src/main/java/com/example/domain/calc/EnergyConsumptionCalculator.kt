package com.example.domain.calc

import kotlin.math.roundToInt

object EnergyConsumptionCalculator {

    data class ConsumptionResult(
        val dailyKwh: Double,
        val monthlyKwh: Double,
        val yearlyKwh: Double,
        val monthlyCost: Double,
        val yearlyCost: Double,
        val steps: List<String>
    )

    fun calculate(
        powerWatts: Double,
        hoursPerDay: Double,
        daysPerMonth: Double = 30.0,
        costPerKwh: Double = 0.18 // Default tariff (e.g. 0.18 SAR per kWh residential tier 1)
    ): ConsumptionResult {
        if (powerWatts <= 0 || hoursPerDay <= 0) {
            throw IllegalArgumentException("يرجى إدخال قيم موجبة للقدرة وساعات التشغيل.")
        }

        val clampedHours = hoursPerDay.coerceIn(0.1, 24.0)
        val clampedDays = daysPerMonth.coerceIn(1.0, 31.0)

        val dailyKwh = (powerWatts * clampedHours) / 1000.0
        val monthlyKwh = dailyKwh * clampedDays
        val yearlyKwh = dailyKwh * 365.0

        val monthlyCost = monthlyKwh * costPerKwh
        val yearlyCost = yearlyKwh * costPerKwh

        val steps = listOf(
            "القدرة الكهربائية: ${round2(powerWatts)} واط (${round2(powerWatts / 1000.0)} ك.و)",
            "ساعات التشغيل: $clampedHours ساعة/يوم على مدار $clampedDays يوم/شهر",
            "الاستهلاك اليومي: ${round2(dailyKwh)} كيلوواط ساعة (kWh)",
            "الاستهلاك الشهري: ${round2(monthlyKwh)} كيلوواط ساعة (kWh)",
            "الاستهلاك السنوي: ${round2(yearlyKwh)} كيلوواط ساعة (kWh)",
            "سعر التعرفة: $costPerKwh لكل كيلوواط ساعة",
            "التكلفة الشهرية التقديرية: ${round2(monthlyCost)}",
            "التكلفة السنوية التقديرية: ${round2(yearlyCost)}"
        )

        return ConsumptionResult(
            dailyKwh = round2(dailyKwh),
            monthlyKwh = round2(monthlyKwh),
            yearlyKwh = round2(yearlyKwh),
            monthlyCost = round2(monthlyCost),
            yearlyCost = round2(yearlyCost),
            steps = steps
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
