package com.example.domain.calc

import kotlin.math.roundToInt
import kotlin.math.sqrt

object LoadScheduleCalculator {

    data class LoadGroup(
        val name: String,
        val connectedPowerKw: Double,
        val demandFactor: Double // e.g. 0.8 for lighting, 0.7 for general sockets
    ) {
        val demandPowerKw: Double get() = connectedPowerKw * demandFactor.coerceIn(0.1, 1.0)
    }

    data class LoadScheduleResult(
        val totalConnectedKw: Double,
        val totalConnectedKva: Double,
        val totalDemandKw: Double,
        val totalDemandKva: Double,
        val estimatedMainCurrentAmps: Double,
        val recommendedMainBreakerAmps: Double,
        val steps: List<String>
    )

    fun calculate(
        loads: List<LoadGroup>,
        voltage: Double = 380.0, // Usually 3-phase for full building/apartment
        isThreePhase: Boolean = true,
        overallPowerFactor: Double = 0.85
    ): LoadScheduleResult {
        if (loads.isEmpty()) {
            throw IllegalArgumentException("يرجى إضافة بند واحد على الأقل لحساب الأحمال.")
        }

        val totalConnectedKw = loads.sumOf { it.connectedPowerKw }
        val totalDemandKw = loads.sumOf { it.demandPowerKw }

        val pf = overallPowerFactor.coerceIn(0.5, 1.0)
        val totalConnectedKva = totalConnectedKw / pf
        val totalDemandKva = totalDemandKw / pf

        // Current calculation based on Maximum Demand
        val currentAmps = if (isThreePhase) {
            (totalDemandKw * 1000.0) / (sqrt(3.0) * voltage * pf)
        } else {
            (totalDemandKw * 1000.0) / (voltage * pf)
        }

        val standardBreakers = listOf(20.0, 25.0, 32.0, 40.0, 50.0, 63.0, 80.0, 100.0, 125.0, 160.0, 200.0, 250.0, 315.0, 400.0, 630.0)
        val minBreaker = currentAmps * 1.25
        val recommendedBreaker = standardBreakers.firstOrNull { it >= minBreaker } ?: standardBreakers.last()

        val steps = mutableListOf<String>()
        steps.add("إجمالي الأحمال الموصولة (Connected Load): ${round2(totalConnectedKw)} ك.و (${round2(totalConnectedKva)} kVA)")
        steps.add("إجمالي أقصى حمل متزامن (Max Demand Load): ${round2(totalDemandKw)} ك.و (${round2(totalDemandKva)} kVA)")
        steps.add("معامل التباين والطلب المتوسط: ${round2((totalDemandKw / totalConnectedKw) * 100.0)} %")
        steps.add("تيار التصميم المحسوب للوحة الرئيسية: ${round2(currentAmps)} أمبير")
        steps.add("القاطع الرئيسي الموصى به: $recommendedBreaker أمبير")

        return LoadScheduleResult(
            totalConnectedKw = round2(totalConnectedKw),
            totalConnectedKva = round2(totalConnectedKva),
            totalDemandKw = round2(totalDemandKw),
            totalDemandKva = round2(totalDemandKva),
            estimatedMainCurrentAmps = round2(currentAmps),
            recommendedMainBreakerAmps = recommendedBreaker,
            steps = steps
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
