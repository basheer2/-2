package com.example.domain.calc

import kotlin.math.roundToInt
import kotlin.math.sqrt

object VoltageDropCalculator {

    enum class ConductorMaterial(val titleAr: String, val resistivity: Double) {
        COPPER("نحاس (Copper)", 0.0178), // ohm*mm^2 / m at 20°C
        ALUMINUM("ألمنيوم (Aluminum)", 0.0282)
    }

    data class VoltageDropResult(
        val voltageDropVolts: Double,
        val voltageDropPercent: Double,
        val voltageAtLoad: Double,
        val isCompliantBranch: Boolean, // <= 3%
        val isCompliantFeeder: Boolean, // <= 5%
        val statusMessage: String,
        val steps: List<String>,
        val formulaExplanation: String
    )

    fun calculate(
        isThreePhase: Boolean,
        nominalVoltage: Double, // e.g. 220V or 380V
        currentAmps: Double,
        lengthMeters: Double,
        wireAreaMm2: Double,
        material: ConductorMaterial = ConductorMaterial.COPPER
    ): VoltageDropResult {
        if (nominalVoltage <= 0 || currentAmps <= 0 || lengthMeters <= 0 || wireAreaMm2 <= 0) {
            throw IllegalArgumentException("جميع المدخلات يجب أن تكون أرقاماً موجبة أكبر من الصفر.")
        }

        // Resistance R = (ρ * L) / A
        // Single Phase: ΔV = 2 * (ρ * L / A) * I
        // Three Phase: ΔV = √3 * (ρ * L / A) * I
        val rho = material.resistivity
        val factor = if (isThreePhase) sqrt(3.0) else 2.0
        val vDrop = (factor * rho * lengthMeters * currentAmps) / wireAreaMm2
        val vDropPercent = (vDrop / nominalVoltage) * 100.0
        val vEnd = nominalVoltage - vDrop

        val isBranchOk = vDropPercent <= 3.0
        val isFeederOk = vDropPercent <= 5.0

        val statusMessage = when {
            vDropPercent <= 3.0 -> "ممتاز: هبوط الجهد ضمن النطاق المثالي الموصى به لدوائر الفروع (≤ 3%)."
            vDropPercent <= 5.0 -> "مقبول: هبوط الجهد ضمن النطاق المسموح به لخطوط التغذية الرئيسية (≤ 5%)."
            else -> "تحذير: هبوط الجهد مرتفع (${round2(vDropPercent)}%) ويتجاوز المعايير الهندسية (5%)! ينصح بزيادة مقطع السلك."
        }

        val formula = if (isThreePhase) {
            "ΔV = (√3 × ρ × L × I) ÷ A"
        } else {
            "ΔV = (2 × ρ × L × I) ÷ A"
        }

        val steps = listOf(
            "المادة: ${material.titleAr} (المقاومة النوعية ρ = $rho Ω·mm²/m)",
            "معامل الطور: ${if (isThreePhase) "ثلاثي الطور (√3 = 1.732)" else "أحادي الطور (2 للذهاب والإياب)"}",
            "مقاومة الخط الإجمالية = ${round4((factor * rho * lengthMeters) / wireAreaMm2)} أوم",
            "هبوط الجهد (ΔV) = ${round2(vDrop)} فولت",
            "نسبة هبوط الجهد (%ΔV) = (${round2(vDrop)} ÷ $nominalVoltage) × 100 = ${round2(vDropPercent)} %",
            "الجهد الفعلي عند الحمل = ${round2(vEnd)} فولت"
        )

        return VoltageDropResult(
            voltageDropVolts = round2(vDrop),
            voltageDropPercent = round2(vDropPercent),
            voltageAtLoad = round2(vEnd),
            isCompliantBranch = isBranchOk,
            isCompliantFeeder = isFeederOk,
            statusMessage = statusMessage,
            steps = steps,
            formulaExplanation = formula
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
    private fun round4(value: Double): Double = (value * 10000.0).roundToInt() / 10000.0
}
