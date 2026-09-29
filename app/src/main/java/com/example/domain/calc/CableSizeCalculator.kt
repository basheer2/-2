package com.example.domain.calc

import kotlin.math.roundToInt
import kotlin.math.sqrt

object CableSizeCalculator {

    enum class InstallationMethod(val titleAr: String, val ratingFactor: Double) {
        CONDUIT_IN_WALL("مواسير مدفونة داخل الجدار (Method B1)", 0.80),
        SURFACE_CONDUIT("مواسير ظاهرة على الجدار (Method B2)", 0.85),
        CABLE_TRAY("على صواني وحوامل كابلات مفتوحة (Method C/E)", 1.00),
        DIRECT_BURIED("مدفون مباشرة في باطن الأرض (Method D)", 0.90)
    }

    enum class InsulationType(val titleAr: String, val maxTemp: String) {
        PVC_70("عزل PVC (حرارة قصوى 70°م)", "70°C"),
        XLPE_90("عزل XLPE (حرارة قصوى 90°م)", "90°C")
    }

    // Standard cross-sections in mm² and their typical nominal current ampacities for Copper in Conduit at 30°C
    data class StandardCable(
        val sizeMm2: Double,
        val copperConduitAmps: Double,
        val copperOpenAmps: Double,
        val alumConduitAmps: Double
    )

    private val STANDARD_SIZES = listOf(
        StandardCable(1.5, 15.5, 19.5, 11.5),
        StandardCable(2.5, 21.0, 27.0, 16.0),
        StandardCable(4.0, 28.0, 36.0, 21.0),
        StandardCable(6.0, 36.0, 46.0, 27.0),
        StandardCable(10.0, 50.0, 63.0, 38.0),
        StandardCable(16.0, 68.0, 85.0, 52.0),
        StandardCable(25.0, 89.0, 112.0, 68.0),
        StandardCable(35.0, 110.0, 138.0, 84.0),
        StandardCable(50.0, 134.0, 168.0, 102.0),
        StandardCable(70.0, 171.0, 213.0, 130.0),
        StandardCable(95.0, 207.0, 258.0, 157.0),
        StandardCable(120.0, 239.0, 299.0, 182.0),
        StandardCable(150.0, 272.0, 344.0, 207.0),
        StandardCable(185.0, 310.0, 392.0, 236.0),
        StandardCable(240.0, 364.0, 461.0, 277.0),
        StandardCable(300.0, 419.0, 530.0, 319.0)
    )

    data class CableSelectionResult(
        val recommendedSizeMm2: Double,
        val designCurrentAmps: Double,
        val cableCurrentCapacityAmps: Double,
        val deratingFactorTotal: Double,
        val calculatedVoltageDropPercent: Double,
        val breakerRecommendationAmps: Double,
        val steps: List<String>,
        val advice: String
    )

    fun calculate(
        loadCurrentAmps: Double,
        voltage: Double = 220.0,
        isThreePhase: Boolean = false,
        lengthMeters: Double = 25.0,
        material: VoltageDropCalculator.ConductorMaterial = VoltageDropCalculator.ConductorMaterial.COPPER,
        installation: InstallationMethod = InstallationMethod.CONDUIT_IN_WALL,
        ambientTempC: Double = 35.0,
        insulation: InsulationType = InsulationType.PVC_70,
        maxAllowedDropPercent: Double = 3.0
    ): CableSelectionResult {
        if (loadCurrentAmps <= 0) throw IllegalArgumentException("تيار الحمل يجب أن يكون أكبر من الصفر.")

        // Temperature derating factor (approximated according to IEC 60364)
        val tempDerating = when {
            ambientTempC <= 30 -> 1.0
            ambientTempC <= 35 -> 0.94
            ambientTempC <= 40 -> 0.87
            ambientTempC <= 45 -> 0.79
            ambientTempC <= 50 -> 0.71
            else -> 0.61
        }

        // Insulation bonus for XLPE (approx +15% capacity)
        val insulationBonus = if (insulation == InsulationType.XLPE_90) 1.15 else 1.0

        val totalDerating = tempDerating * installation.ratingFactor * insulationBonus
        // Required base capacity:
        val requiredBaseCurrent = loadCurrentAmps / totalDerating

        // Find candidate by thermal ampacity
        var selectedCable: StandardCable? = null
        for (cable in STANDARD_SIZES) {
            val baseCapacity = when (material) {
                VoltageDropCalculator.ConductorMaterial.COPPER -> {
                    if (installation == InstallationMethod.CABLE_TRAY) cable.copperOpenAmps else cable.copperConduitAmps
                }
                VoltageDropCalculator.ConductorMaterial.ALUMINUM -> cable.alumConduitAmps
            }
            if (baseCapacity >= requiredBaseCurrent) {
                // Check voltage drop constraint
                val factor = if (isThreePhase) sqrt(3.0) else 2.0
                val vd = (factor * material.resistivity * lengthMeters * loadCurrentAmps) / cable.sizeMm2
                val vdPercent = (vd / voltage) * 100.0

                if (vdPercent <= maxAllowedDropPercent) {
                    selectedCable = cable
                    break
                }
            }
        }

        // If none met both, pick largest available
        if (selectedCable == null) {
            selectedCable = STANDARD_SIZES.last()
        }

        val factor = if (isThreePhase) sqrt(3.0) else 2.0
        val finalVd = (factor * material.resistivity * lengthMeters * loadCurrentAmps) / selectedCable.sizeMm2
        val finalVdPercent = (finalVd / voltage) * 100.0

        val nominalBaseCapacity = if (material == VoltageDropCalculator.ConductorMaterial.COPPER) {
            if (installation == InstallationMethod.CABLE_TRAY) selectedCable.copperOpenAmps else selectedCable.copperConduitAmps
        } else {
            selectedCable.alumConduitAmps
        }
        val actualEffectiveCapacity = nominalBaseCapacity * totalDerating

        // Standard circuit breaker rating
        val standardBreakers = listOf(6.0, 10.0, 16.0, 20.0, 25.0, 32.0, 40.0, 50.0, 63.0, 80.0, 100.0, 125.0, 160.0, 200.0, 250.0, 315.0, 400.0)
        val minBreaker = loadCurrentAmps * 1.25
        val recommendedBreaker = standardBreakers.firstOrNull { it >= minBreaker } ?: standardBreakers.last()

        val steps = listOf(
            "تيار الحمل المطلوب: ${round2(loadCurrentAmps)} أمبير",
            "معامل تصحيح درجة الحرارة ($ambientTempC°م): ${round2(tempDerating)}",
            "معامل طريقة التمديد (${installation.titleAr}): ${installation.ratingFactor}",
            "معامل تصحيح العزل (${insulation.titleAr}): $insulationBonus",
            "معامل التصحيح الكلي: ${round2(totalDerating)}",
            "السعة الحرارية المطلوبة للكابل في الظروف القياسية: ${round2(requiredBaseCurrent)} أمبير",
            "مقطع الكابل المناسب: ${selectedCable.sizeMm2} ملم²",
            "تحمل الكابل الفعلي في ظروف التشغيل: ${round2(actualEffectiveCapacity)} أمبير",
            "هبوط الجهد المحسوب لطول $lengthMeters م: ${round2(finalVdPercent)} % (الحد المسموح $maxAllowedDropPercent %)",
            "القاطع المقترح لحماية الدائرة: $recommendedBreaker أمبير"
        )

        val advice = if (finalVdPercent > maxAllowedDropPercent) {
            "تنبيه: مسافة التمديد طويلة مما سبب هبوط جهد أعلى من المطلوب، يرجى التفكير في زيادة مقطع الكابل للمقاس الأكبر."
        } else {
            "المقاس الموصى به يلبي متطلبات التحمل الحراري وهبوط الجهد وفق المعايير الكهربائية المعتمدة."
        }

        return CableSelectionResult(
            recommendedSizeMm2 = selectedCable.sizeMm2,
            designCurrentAmps = round2(requiredBaseCurrent),
            cableCurrentCapacityAmps = round2(actualEffectiveCapacity),
            deratingFactorTotal = round2(totalDerating),
            calculatedVoltageDropPercent = round2(finalVdPercent),
            breakerRecommendationAmps = recommendedBreaker,
            steps = steps,
            advice = advice
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
