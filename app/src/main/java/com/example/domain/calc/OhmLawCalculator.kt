package com.example.domain.calc

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object OhmLawCalculator {

    data class Result(
        val voltage: Double,
        val current: Double,
        val resistance: Double,
        val power: Double,
        val formulaExplanation: String,
        val steps: List<String>
    )

    /**
     * Calculates two unknown values from two provided values.
     * Pass null or <= 0 for unknown values.
     */
    fun calculate(
        v: Double? = null,
        i: Double? = null,
        r: Double? = null,
        p: Double? = null
    ): Result {
        val hasV = v != null && v > 0
        val hasI = i != null && i > 0
        val hasR = r != null && r > 0
        val hasP = p != null && p > 0

        val knownCount = listOf(hasV, hasI, hasR, hasP).count { it }
        if (knownCount < 2) {
            throw IllegalArgumentException("يرجى إدخال قيمتين معلومتين على الأقل لإجراء الحساب.")
        }

        var resV = 0.0
        var resI = 0.0
        var resR = 0.0
        var resP = 0.0
        val steps = mutableListOf<String>()
        var formula = ""

        when {
            hasV && hasI -> {
                resV = v!!
                resI = i!!
                resR = resV / resI
                resP = resV * resI
                formula = "R = V / I   |   P = V × I"
                steps.add("المقاومة (R) = $resV ÷ $resI = ${round2(resR)} أوم (Ω)")
                steps.add("القدرة (P) = $resV × $resI = ${round2(resP)} واط (W)")
            }
            hasV && hasR -> {
                resV = v!!
                resR = r!!
                resI = resV / resR
                resP = (resV * resV) / resR
                formula = "I = V / R   |   P = V² / R"
                steps.add("التيار (I) = $resV ÷ $resR = ${round2(resI)} أمبير (A)")
                steps.add("القدرة (P) = ($resV)² ÷ $resR = ${round2(resP)} واط (W)")
            }
            hasV && hasP -> {
                resV = v!!
                resP = p!!
                resI = resP / resV
                resR = (resV * resV) / resP
                formula = "I = P / V   |   R = V² / P"
                steps.add("التيار (I) = $resP ÷ $resV = ${round2(resI)} أمبير (A)")
                steps.add("المقاومة (R) = ($resV)² ÷ $resP = ${round2(resR)} أوم (Ω)")
            }
            hasI && hasR -> {
                resI = i!!
                resR = r!!
                resV = resI * resR
                resP = resI * resI * resR
                formula = "V = I × R   |   P = I² × R"
                steps.add("الجهد (V) = $resI × $resR = ${round2(resV)} فولت (V)")
                steps.add("القدرة (P) = ($resI)² × $resR = ${round2(resP)} واط (W)")
            }
            hasI && hasP -> {
                resI = i!!
                resP = p!!
                resV = resP / resI
                resR = resP / (resI * resI)
                formula = "V = P / I   |   R = P / I²"
                steps.add("الجهد (V) = $resP ÷ $resI = ${round2(resV)} فولت (V)")
                steps.add("المقاومة (R) = $resP ÷ ($resI)² = ${round2(resR)} أوم (Ω)")
            }
            hasR && hasP -> {
                resR = r!!
                resP = p!!
                resV = sqrt(resP * resR)
                resI = sqrt(resP / resR)
                formula = "V = √(P × R)   |   I = √(P / R)"
                steps.add("الجهد (V) = √($resP × $resR) = ${round2(resV)} فولت (V)")
                steps.add("التيار (I) = √($resP ÷ $resR) = ${round2(resI)} أمبير (A)")
            }
        }

        return Result(
            voltage = round2(resV),
            current = round2(resI),
            resistance = round2(resR),
            power = round2(resP),
            formulaExplanation = formula,
            steps = steps
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
