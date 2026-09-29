package com.example.domain.calc

import kotlin.math.roundToInt

object BreakerSizeCalculator {

    enum class LoadType(val titleAr: String, val continuousMultiplier: Double) {
        CONTINUOUS("حمل مستمر لأكثر من 3 ساعات (تكييف، إضاءة تجارية، سخانات)", 1.25),
        NON_CONTINUOUS("حمل غير مستمر أو أحمال عادية (أفياش عامة، ورش متقطعة)", 1.00),
        MOTOR_START("أحمال محركات وحثية (مضخات، مصاعد، كمبروسر)", 1.50)
    }

    enum class BreakerType(val titleAr: String) {
        MCB("قاطع تفاضلي منمنم (MCB - Miniature Circuit Breaker)"),
        MCCB("قاطع مقولب صناعي (MCCB - Molded Case Circuit Breaker)"),
        ELCB("قاطع حماية من التسريب الأرضي (ELCB / RCCB 30mA)")
    }

    private val STANDARD_RATINGS = listOf(
        6.0, 10.0, 16.0, 20.0, 25.0, 32.0, 40.0, 50.0, 63.0,
        80.0, 100.0, 125.0, 160.0, 200.0, 250.0, 315.0, 400.0, 500.0, 630.0, 800.0
    )

    data class BreakerResult(
        val recommendedRatingAmps: Double,
        val minimumDesignAmps: Double,
        val suggestedType: BreakerType,
        val polesRecommendation: String,
        val steps: List<String>,
        val technicalNote: String
    )

    fun calculate(
        loadAmps: Double,
        isThreePhase: Boolean = false,
        loadType: LoadType = LoadType.CONTINUOUS
    ): BreakerResult {
        if (loadAmps <= 0) throw IllegalArgumentException("قيمة تيار الحمل يجب أن تكون أكبر من الصفر.")

        val minDesignCurrent = loadAmps * loadType.continuousMultiplier
        val recommendedRating = STANDARD_RATINGS.firstOrNull { it >= minDesignCurrent } ?: STANDARD_RATINGS.last()

        val breakerType = if (recommendedRating <= 100.0) BreakerType.MCB else BreakerType.MCCB
        val poles = if (isThreePhase) "3 أقطاب (3P) أو 4 أقطاب (4P مع نيوترال)" else "قطب واحد (1P) أو قطبين (2P)"

        val steps = listOf(
            "تيار الحمل الاسمي: ${round2(loadAmps)} أمبير",
            "طبيعة الحمل: ${loadType.titleAr}",
            "معامل الأمان المطلوب: ×${loadType.continuousMultiplier}",
            "الحد الأدنى لسعة القاطع التصميمية: ${round2(minDesignCurrent)} أمبير",
            "السعة القياسية المناسبة المختارة: $recommendedRating أمبير",
            "نوع القاطع المقترح: ${breakerType.titleAr}",
            "عدد الأقطاب: $poles"
        )

        val note = "قاعدة الكود الكهربائي (NEC / IEC): لا يجوز تحميل القواطع المستمرة بأكثر من 80% من سعتها الاسمية، لذلك يُضرب الحمل المستمر بمعامل 1.25 لضمان عدم الفصل الحراري الخاطئ."

        return BreakerResult(
            recommendedRatingAmps = recommendedRating,
            minimumDesignAmps = round2(minDesignCurrent),
            suggestedType = breakerType,
            polesRecommendation = poles,
            steps = steps,
            technicalNote = note
        )
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
