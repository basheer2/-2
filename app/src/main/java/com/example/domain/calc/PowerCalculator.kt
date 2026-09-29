package com.example.domain.calc

import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object PowerCalculator {

    enum class PhaseType(val titleAr: String) {
        SINGLE_PHASE("أحادي الطور (1-Phase - 220V/110V)"),
        THREE_PHASE("ثلاثي الطور (3-Phase - 380V/400V)")
    }

    data class PowerResult(
        val activePowerKw: Double, // kW
        val apparentPowerKva: Double, // kVA
        val reactivePowerKvar: Double, // kVAR
        val currentAmperes: Double, // A
        val powerFactor: Double, // cos φ
        val steps: List<String>,
        val formulaExplanation: String
    )

    /**
     * Calculate power or current.
     * Can calculate when given:
     * - voltage, powerFactor, and either (activePowerKw) OR (currentAmperes).
     */
    fun calculate(
        phaseType: PhaseType,
        voltage: Double,
        powerFactor: Double = 0.85,
        activePowerKw: Double? = null,
        currentAmperes: Double? = null
    ): PowerResult {
        if (voltage <= 0) throw IllegalArgumentException("قيمة الجهد يجب أن تكون أكبر من الصفر.")
        val pf = powerFactor.coerceIn(0.1, 1.0)
        val steps = mutableListOf<String>()

        val kw: Double
        val amps: Double

        if (activePowerKw != null && activePowerKw > 0) {
            kw = activePowerKw
            amps = if (phaseType == PhaseType.SINGLE_PHASE) {
                // I = (P * 1000) / (V * cosφ)
                (kw * 1000.0) / (voltage * pf)
            } else {
                // I = (P * 1000) / (√3 * V * cosφ)
                (kw * 1000.0) / (sqrt(3.0) * voltage * pf)
            }
        } else if (currentAmperes != null && currentAmperes > 0) {
            amps = currentAmperes
            kw = if (phaseType == PhaseType.SINGLE_PHASE) {
                // P = (V * I * cosφ) / 1000
                (voltage * amps * pf) / 1000.0
            } else {
                // P = (√3 * V * I * cosφ) / 1000
                (sqrt(3.0) * voltage * amps * pf) / 1000.0
            }
        } else {
            throw IllegalArgumentException("يرجى إدخال إما القدرة (kW) أو التيار (A).")
        }

        // Apparent Power S (kVA) = P / cosφ
        val kva = kw / pf
        // Reactive Power Q (kVAR) = √(S² - P²) or S * sin(acos(pf))
        val phi = acos(pf)
        val kvar = kva * sin(phi)

        val formula = if (phaseType == PhaseType.SINGLE_PHASE) {
            "P (kW) = (V × I × cosφ) ÷ 1000   |   I = (P × 1000) ÷ (V × cosφ)"
        } else {
            "P (kW) = (√3 × V × I × cosφ) ÷ 1000   |   I = (P × 1000) ÷ (1.732 × V × cosφ)"
        }

        steps.add("نوع النظام: ${phaseType.titleAr}")
        steps.add("الجهد الاسمي: $voltage فولت | معامل القدرة (cosφ): $pf")
        if (phaseType == PhaseType.SINGLE_PHASE) {
            steps.add("التيار المحسوب = (${round2(kw)} × 1000) ÷ ($voltage × $pf) = ${round2(amps)} أمبير")
        } else {
            steps.add("التيار المحسوب = (${round2(kw)} × 1000) ÷ (1.732 × $voltage × $pf) = ${round2(amps)} أمبير لكل فاز")
        }
        steps.add("القدرة الظاهرية (S) = ${round2(kw)} ÷ $pf = ${round2(kva)} ك.ف.أ (kVA)")
        steps.add("القدرة غير الفعالة (Q) = √(${round2(kva)}² - ${round2(kw)}²) = ${round2(kvar)} ك.ف.أ.ر (kVAR)")

        return PowerResult(
            activePowerKw = round2(kw),
            apparentPowerKva = round2(kva),
            reactivePowerKvar = round2(kvar),
            currentAmperes = round2(amps),
            powerFactor = pf,
            steps = steps,
            formulaExplanation = formula
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
