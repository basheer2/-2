package com.example.ui.screens.calculators

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calc.BreakerSizeCalculator
import com.example.domain.calc.CableSizeCalculator
import com.example.domain.calc.EnergyConsumptionCalculator
import com.example.domain.calc.LoadScheduleCalculator
import com.example.domain.calc.OhmLawCalculator
import com.example.domain.calc.PowerCalculator
import com.example.domain.calc.UnitConverter
import com.example.domain.calc.VoltageDropCalculator
import com.example.ui.components.UnitNumberField
import com.example.ui.theme.AmberGoldPrimary
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EnergyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.ElectricianViewModel

enum class CalcTab(val id: String, val titleAr: String) {
    OHM("ohm", "قانون أوم"),
    POWER("power", "القدرة"),
    VOLTAGE_DROP("voltage_drop", "هبوط الجهد"),
    CABLE_SIZE("cable_size", "مقطع الكابل"),
    BREAKER("breaker", "القواطع"),
    CONSUMPTION("consumption", "الاستهلاك"),
    LOADS("loads", "الأحمال"),
    CONVERTER("converter", "المحوّل")
}

@Composable
fun CalculatorsScreen(
    viewModel: ElectricianViewModel,
    initialTab: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember {
        mutableStateOf(
            CalcTab.values().firstOrNull { it.id == initialTab } ?: CalcTab.OHM
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculators_screen")
    ) {
        // Tab Chips Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CalcTab.values()) { tab ->
                val isSelected = tab == selectedTab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = {
                        Text(
                            text = tab.titleAr,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Calculator View
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                CalcTab.OHM -> OhmCalculatorView(viewModel)
                CalcTab.POWER -> PowerCalculatorView(viewModel)
                CalcTab.VOLTAGE_DROP -> VoltageDropCalculatorView(viewModel)
                CalcTab.CABLE_SIZE -> CableSizeCalculatorView(viewModel)
                CalcTab.BREAKER -> BreakerCalculatorView(viewModel)
                CalcTab.CONSUMPTION -> ConsumptionCalculatorView(viewModel)
                CalcTab.LOADS -> LoadScheduleCalculatorView(viewModel)
                CalcTab.CONVERTER -> UnitConverterView(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Ohm's Law Calculator View
// -------------------------------------------------------------
@Composable
fun OhmCalculatorView(viewModel: ElectricianViewModel) {
    var vInput by remember { mutableStateOf("220") }
    var iInput by remember { mutableStateOf("10") }
    var rInput by remember { mutableStateOf("") }
    var pInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<OhmLawCalculator.Result?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSteps by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "💡 أدخل أي قيمتين معلومتين، وسيقوم النظام بحساب القيمتين الأخريين تلقائياً.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        item {
            UnitNumberField(value = vInput, onValueChange = { vInput = it }, label = "الجهد الكهربائي (V)", unit = "فولت")
        }
        item {
            UnitNumberField(value = iInput, onValueChange = { iInput = it }, label = "شدة التيار (I)", unit = "أمبير")
        }
        item {
            UnitNumberField(value = rInput, onValueChange = { rInput = it }, label = "المقاومة الكهربائية (R)", unit = "أوم Ω")
        }
        item {
            UnitNumberField(value = pInput, onValueChange = { pInput = it }, label = "القدرة الكهربائية (P)", unit = "واط W")
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        try {
                            errorMessage = null
                            val v = vInput.toDoubleOrNull()
                            val i = iInput.toDoubleOrNull()
                            val r = rInput.toDoubleOrNull()
                            val p = pInput.toDoubleOrNull()
                            result = OhmLawCalculator.calculate(v, i, r, p)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "يرجى التحقق من المدخلات"
                            result = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("احسب الآن", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        vInput = ""
                        iInput = ""
                        rInput = ""
                        pInput = ""
                        result = null
                        errorMessage = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح")
                }
            }
        }

        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage!!,
                    color = DangerRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        if (result != null) {
            item {
                CalculatorResultCard(
                    title = "نتائج قانون أوم",
                    metrics = listOf(
                        Pair("الجهد (V)", "${result!!.voltage} فولت"),
                        Pair("التيار (I)", "${result!!.current} أمبير"),
                        Pair("المقاومة (R)", "${result!!.resistance} أوم (Ω)"),
                        Pair("القدرة (P)", "${result!!.power} واط (${result!!.power / 1000.0} ك.و)")
                    ),
                    formula = result!!.formulaExplanation,
                    steps = result!!.steps,
                    showSteps = showSteps,
                    onToggleSteps = { showSteps = !showSteps },
                    onCopy = {
                        val text = "نتائج قانون أوم:\nالجهد: ${result!!.voltage}V | التيار: ${result!!.current}A\nالمقاومة: ${result!!.resistance}Ω | القدرة: ${result!!.power}W"
                        copyToClipboard(context, text)
                    },
                    onSave = {
                        viewModel.saveCalculationRecord(
                            title = "قانون أوم",
                            category = "ohm",
                            inputSummary = "V=${result!!.voltage}V, I=${result!!.current}A",
                            resultSummary = "R=${result!!.resistance}Ω, P=${result!!.power}W",
                            formula = result!!.formulaExplanation
                        )
                    }
                )
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 2. Power Calculator View
// -------------------------------------------------------------
@Composable
fun PowerCalculatorView(viewModel: ElectricianViewModel) {
    var isThreePhase by remember { mutableStateOf(false) }
    var voltageInput by remember { mutableStateOf("220") }
    var powerFactorInput by remember { mutableStateOf("0.85") }
    var kwInput by remember { mutableStateOf("5.5") }
    var ampsInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<PowerCalculator.PowerResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSteps by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isThreePhase,
                    onClick = {
                        isThreePhase = false
                        if (voltageInput == "380") voltageInput = "220"
                    },
                    label = { Text("أحادي الطور 1-Phase (220V)") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = isThreePhase,
                    onClick = {
                        isThreePhase = true
                        if (voltageInput == "220") voltageInput = "380"
                    },
                    label = { Text("ثلاثي الطور 3-Phase (380V)") },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        item {
            UnitNumberField(value = voltageInput, onValueChange = { voltageInput = it }, label = "الجهد الاسمي (V)", unit = "فولت")
        }
        item {
            UnitNumberField(value = powerFactorInput, onValueChange = { powerFactorInput = it }, label = "معامل القدرة (cos φ)", unit = "0.1 - 1.0")
        }
        item {
            UnitNumberField(value = kwInput, onValueChange = { kwInput = it; if (it.isNotBlank()) ampsInput = "" }, label = "القدرة الفعالة (P)", unit = "كيلوواط kW")
        }
        item {
            UnitNumberField(value = ampsInput, onValueChange = { ampsInput = it; if (it.isNotBlank()) kwInput = "" }, label = "أو أدخل التيار لمعرفة القدرة (I)", unit = "أمبير A")
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        try {
                            errorMessage = null
                            val v = voltageInput.toDoubleOrNull() ?: 220.0
                            val pf = powerFactorInput.toDoubleOrNull() ?: 0.85
                            val kw = kwInput.toDoubleOrNull()
                            val amps = ampsInput.toDoubleOrNull()
                            val phase = if (isThreePhase) PowerCalculator.PhaseType.THREE_PHASE else PowerCalculator.PhaseType.SINGLE_PHASE
                            result = PowerCalculator.calculate(phase, v, pf, kw, amps)
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "خطأ في المدخلات"
                            result = null
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("احسب القدرة", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        kwInput = ""
                        ampsInput = ""
                        result = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("مسح")
                }
            }
        }

        errorMessage?.let { msg ->
            item { Text(msg, color = DangerRed, fontSize = 13.sp) }
        }

        if (result != null) {
            item {
                CalculatorResultCard(
                    title = "نتائج حساب القدرة الكهربائية",
                    metrics = listOf(
                        Pair("التيار المحسوب", "${result!!.currentAmperes} أمبير"),
                        Pair("القدرة الفعالة (P)", "${result!!.activePowerKw} kW"),
                        Pair("القدرة الظاهرية (S)", "${result!!.apparentPowerKva} kVA"),
                        Pair("القدرة غير الفعالة (Q)", "${result!!.reactivePowerKvar} kVAR")
                    ),
                    formula = result!!.formulaExplanation,
                    steps = result!!.steps,
                    showSteps = showSteps,
                    onToggleSteps = { showSteps = !showSteps },
                    onCopy = {
                        copyToClipboard(context, "التيار: ${result!!.currentAmperes}A | القدرة: ${result!!.activePowerKw}kW (${result!!.apparentPowerKva}kVA)")
                    },
                    onSave = {
                        viewModel.saveCalculationRecord(
                            title = "حساب القدرة (${if (isThreePhase) "3 فاز" else "1 فاز"})",
                            category = "power",
                            inputSummary = "V=$voltageInput, pf=$powerFactorInput",
                            resultSummary = "I=${result!!.currentAmperes}A, P=${result!!.activePowerKw}kW, S=${result!!.apparentPowerKva}kVA",
                            formula = result!!.formulaExplanation
                        )
                    }
                )
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 3. Voltage Drop Calculator View
// -------------------------------------------------------------
@Composable
fun VoltageDropCalculatorView(viewModel: ElectricianViewModel) {
    var isThreePhase by remember { mutableStateOf(false) }
    var voltageInput by remember { mutableStateOf("220") }
    var currentInput by remember { mutableStateOf("20") }
    var lengthInput by remember { mutableStateOf("35") }
    var areaInput by remember { mutableStateOf("4") }
    var isCopper by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<VoltageDropCalculator.VoltageDropResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSteps by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isThreePhase,
                    onClick = { isThreePhase = false; if (voltageInput == "380") voltageInput = "220" },
                    label = { Text("أحادي 1-Phase") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = isThreePhase,
                    onClick = { isThreePhase = true; if (voltageInput == "220") voltageInput = "380" },
                    label = { Text("ثلاثي 3-Phase") },
                    shape = RoundedCornerShape(10.dp)
                )
                FilterChip(
                    selected = isCopper,
                    onClick = { isCopper = !isCopper },
                    label = { Text(if (isCopper) "سلك نحاس" else "سلك ألمنيوم") },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        item {
            UnitNumberField(value = voltageInput, onValueChange = { voltageInput = it }, label = "الجهد الاسمي", unit = "فولت V")
        }
        item {
            UnitNumberField(value = currentInput, onValueChange = { currentInput = it }, label = "تيار الحمل المار بالدائرة", unit = "أمبير A")
        }
        item {
            UnitNumberField(value = lengthInput, onValueChange = { lengthInput = it }, label = "طول خط التغذية (المسافة)", unit = "متر m")
        }
        item {
            UnitNumberField(value = areaInput, onValueChange = { areaInput = it }, label = "مساحة مقطع السلك", unit = "ملم² mm²")
        }

        item {
            Button(
                onClick = {
                    try {
                        errorMessage = null
                        val v = voltageInput.toDoubleOrNull() ?: 220.0
                        val i = currentInput.toDoubleOrNull() ?: 20.0
                        val l = lengthInput.toDoubleOrNull() ?: 35.0
                        val a = areaInput.toDoubleOrNull() ?: 4.0
                        val mat = if (isCopper) VoltageDropCalculator.ConductorMaterial.COPPER else VoltageDropCalculator.ConductorMaterial.ALUMINUM
                        result = VoltageDropCalculator.calculate(isThreePhase, v, i, l, a, mat)
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "خطأ في المدخلات"
                        result = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حساب هبوط الجهد", fontWeight = FontWeight.Bold)
            }
        }

        errorMessage?.let { msg ->
            item { Text(msg, color = DangerRed, fontSize = 13.sp) }
        }

        if (result != null) {
            item {
                val statusColor = if (result!!.isCompliantBranch) EnergyGreen else if (result!!.isCompliantFeeder) WarningOrange else DangerRed
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (result!!.isCompliantFeeder) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "هبوط الجهد: ${result!!.voltageDropVolts} فولت (${result!!.voltageDropPercent} %)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result!!.statusMessage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "الجهد المتبقي عند نهاية الخط: ${result!!.voltageAtLoad} فولت",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showSteps = !showSteps },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (showSteps) "إخفاء الخطوات" else "عرض الخطوات")
                            }
                            IconButton(onClick = {
                                copyToClipboard(context, "هبوط الجهد: ${result!!.voltageDropVolts}V (${result!!.voltageDropPercent}%)")
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ")
                            }
                            IconButton(onClick = {
                                viewModel.saveCalculationRecord(
                                    title = "هبوط الجهد ($lengthInput م، $areaInput ملم²)",
                                    category = "voltage_drop",
                                    inputSummary = "I=$currentInput A, L=$lengthInput m, A=$areaInput mm²",
                                    resultSummary = "ΔV=${result!!.voltageDropVolts}V (${result!!.voltageDropPercent}%)",
                                    formula = result!!.formulaExplanation
                                )
                            }) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "حفظ")
                            }
                        }

                        if (showSteps) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            result!!.steps.forEach { step ->
                                Text("• $step", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 4. Cable Size Selector View
// -------------------------------------------------------------
@Composable
fun CableSizeCalculatorView(viewModel: ElectricianViewModel) {
    var loadCurrentInput by remember { mutableStateOf("25") }
    var lengthInput by remember { mutableStateOf("30") }
    var voltageInput by remember { mutableStateOf("220") }
    var isThreePhase by remember { mutableStateOf(false) }
    var tempInput by remember { mutableStateOf("40") }
    var selectedMethod by remember { mutableStateOf(CableSizeCalculator.InstallationMethod.CONDUIT_IN_WALL) }
    var result by remember { mutableStateOf<CableSizeCalculator.CableSelectionResult?>(null) }
    var showSteps by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            UnitNumberField(value = loadCurrentInput, onValueChange = { loadCurrentInput = it }, label = "تيار الحمل التصميمي (I)", unit = "أمبير A")
        }
        item {
            UnitNumberField(value = lengthInput, onValueChange = { lengthInput = it }, label = "طول المسار (المسافة)", unit = "متر m")
        }
        item {
            UnitNumberField(value = tempInput, onValueChange = { tempInput = it }, label = "درجة الحرارة المحيطة القصوى", unit = "°م")
        }

        // Installation method selector
        item {
            Text("طريقة تمديد الكابل:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            CableSizeCalculator.InstallationMethod.values().forEach { method ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method.titleAr, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val amps = loadCurrentInput.toDoubleOrNull() ?: 25.0
                    val len = lengthInput.toDoubleOrNull() ?: 30.0
                    val v = voltageInput.toDoubleOrNull() ?: 220.0
                    val t = tempInput.toDoubleOrNull() ?: 40.0
                    result = CableSizeCalculator.calculate(
                        loadCurrentAmps = amps,
                        voltage = v,
                        isThreePhase = isThreePhase,
                        lengthMeters = len,
                        installation = selectedMethod,
                        ambientTempC = t
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تحديد مقطع الكابل المناسب", fontWeight = FontWeight.Bold)
            }
        }

        if (result != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "المقطع الموصى به: ${result!!.recommendedSizeMm2} ملم² نحاس",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result!!.advice,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "سعة الكابل الفعلية: ${result!!.cableCurrentCapacityAmps} أمبير", fontSize = 13.sp)
                        Text(text = "هبوط الجهد المتوقع: ${result!!.calculatedVoltageDropPercent} %", fontSize = 13.sp)
                        Text(text = "القاطع المناسب للدائرة: ${result!!.breakerRecommendationAmps} أمبير", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showSteps = !showSteps }) {
                                Text(if (showSteps) "إخفاء التفاصيل" else "تفاصيل الحساب")
                            }
                            IconButton(onClick = {
                                copyToClipboard(context, "مقطع الكابل الموصى به: ${result!!.recommendedSizeMm2} mm² | قاطع: ${result!!.breakerRecommendationAmps}A")
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                            IconButton(onClick = {
                                viewModel.saveCalculationRecord(
                                    title = "اختيار مقطع كابل (${result!!.recommendedSizeMm2} مم²)",
                                    category = "cable_size",
                                    inputSummary = "I=$loadCurrentInput A, L=$lengthInput m, T=$tempInput°C",
                                    resultSummary = "المقطع=${result!!.recommendedSizeMm2} ملم²، قاطع=${result!!.breakerRecommendationAmps} A"
                                )
                            }) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                            }
                        }

                        if (showSteps) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            result!!.steps.forEach { step ->
                                Text("• $step", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 5. Circuit Breaker Sizing View
// -------------------------------------------------------------
@Composable
fun BreakerCalculatorView(viewModel: ElectricianViewModel) {
    var loadCurrentInput by remember { mutableStateOf("28") }
    var isThreePhase by remember { mutableStateOf(false) }
    var selectedLoadType by remember { mutableStateOf(BreakerSizeCalculator.LoadType.CONTINUOUS) }
    var result by remember { mutableStateOf<BreakerSizeCalculator.BreakerResult?>(null) }
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            UnitNumberField(value = loadCurrentInput, onValueChange = { loadCurrentInput = it }, label = "تيار الحمل الفعلي (Amps)", unit = "A")
        }

        item {
            Text("طبيعة الحمل:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            BreakerSizeCalculator.LoadType.values().forEach { type ->
                FilterChip(
                    selected = selectedLoadType == type,
                    onClick = { selectedLoadType = type },
                    label = { Text(type.titleAr, fontSize = 12.sp) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        item {
            Button(
                onClick = {
                    val amps = loadCurrentInput.toDoubleOrNull() ?: 28.0
                    result = BreakerSizeCalculator.calculate(amps, isThreePhase, selectedLoadType)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حساب سعة القاطع", fontWeight = FontWeight.Bold)
            }
        }

        if (result != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "القاطع المقترح: ${result!!.recommendedRatingAmps} أمبير",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = result!!.suggestedType.titleAr, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "الأقطاب: ${result!!.polesRecommendation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = result!!.technicalNote, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row {
                            IconButton(onClick = {
                                copyToClipboard(context, "القاطع الموصى به: ${result!!.recommendedRatingAmps}A (${result!!.suggestedType.titleAr})")
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                            IconButton(onClick = {
                                viewModel.saveCalculationRecord(
                                    title = "حساب قاطع (${result!!.recommendedRatingAmps}A)",
                                    category = "breaker",
                                    inputSummary = "تيار=$loadCurrentInput A",
                                    resultSummary = "قاطع=${result!!.recommendedRatingAmps}A - ${result!!.suggestedType.name}"
                                )
                            }) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 6. Energy Consumption View
// -------------------------------------------------------------
@Composable
fun ConsumptionCalculatorView(viewModel: ElectricianViewModel) {
    var wattsInput by remember { mutableStateOf("2200") } // e.g. Water heater or 1.5 HP AC
    var hoursInput by remember { mutableStateOf("8") }
    var tariffInput by remember { mutableStateOf("0.18") }
    var result by remember { mutableStateOf<EnergyConsumptionCalculator.ConsumptionResult?>(null) }
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            UnitNumberField(value = wattsInput, onValueChange = { wattsInput = it }, label = "قدرة الجهاز (Watts)", unit = "واط W")
        }
        item {
            UnitNumberField(value = hoursInput, onValueChange = { hoursInput = it }, label = "ساعات التشغيل باليوم", unit = "ساعة/يوم")
        }
        item {
            UnitNumberField(value = tariffInput, onValueChange = { tariffInput = it }, label = "سعر التعرفة لكل kWh", unit = "عملة/kWh")
        }

        item {
            Button(
                onClick = {
                    val w = wattsInput.toDoubleOrNull() ?: 2200.0
                    val h = hoursInput.toDoubleOrNull() ?: 8.0
                    val t = tariffInput.toDoubleOrNull() ?: 0.18
                    result = EnergyConsumptionCalculator.calculate(w, h, costPerKwh = t)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حساب التكلفة والاستهلاك", fontWeight = FontWeight.Bold)
            }
        }

        if (result != null) {
            item {
                CalculatorResultCard(
                    title = "تقدير استهلاك الطاقة والتكلفة",
                    metrics = listOf(
                        Pair("الاستهلاك الشهري", "${result!!.monthlyKwh} kWh"),
                        Pair("التكلفة الشهرية", "${result!!.monthlyCost}"),
                        Pair("الاستهلاك السنوي", "${result!!.yearlyKwh} kWh"),
                        Pair("التكلفة السنوية", "${result!!.yearlyCost}")
                    ),
                    formula = "kWh = (واط × ساعات) ÷ 1000",
                    steps = result!!.steps,
                    showSteps = true,
                    onToggleSteps = {},
                    onCopy = {
                        copyToClipboard(context, "الاستهلاك: ${result!!.monthlyKwh} kWh/شهر | التكلفة: ${result!!.monthlyCost}")
                    },
                    onSave = {
                        viewModel.saveCalculationRecord(
                            title = "حساب استهلاك كهرباء",
                            category = "consumption",
                            inputSummary = "$wattsInput واط، $hoursInput ساعة/يوم",
                            resultSummary = "${result!!.monthlyKwh} kWh/شهر (${result!!.monthlyCost})"
                        )
                    }
                )
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 7. Load Schedule View
// -------------------------------------------------------------
@Composable
fun LoadScheduleCalculatorView(viewModel: ElectricianViewModel) {
    var lightingKw by remember { mutableStateOf("3.0") }
    var socketsKw by remember { mutableStateOf("6.0") }
    var hvacKw by remember { mutableStateOf("12.0") }
    var waterHeatersKw by remember { mutableStateOf("4.5") }
    var result by remember { mutableStateOf<LoadScheduleCalculator.LoadScheduleResult?>(null) }
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("أدخل أحمال المشروع بالكيلوواط (kW) لحساب أقصى طلب والقاطع الرئيسي:", fontSize = 13.sp)
        }
        item {
            UnitNumberField(value = lightingKw, onValueChange = { lightingKw = it }, label = "أحمال الإنارة (معامل طلب 0.8)", unit = "kW")
        }
        item {
            UnitNumberField(value = socketsKw, onValueChange = { socketsKw = it }, label = "أحمال الأفياش والمقابس العامة (معامل 0.6)", unit = "kW")
        }
        item {
            UnitNumberField(value = hvacKw, onValueChange = { hvacKw = it }, label = "أحمال التكييف والتهوية (معامل 0.9)", unit = "kW")
        }
        item {
            UnitNumberField(value = waterHeatersKw, onValueChange = { waterHeatersKw = it }, label = "السخانات والمضخات (معامل 0.7)", unit = "kW")
        }

        item {
            Button(
                onClick = {
                    val lKw = lightingKw.toDoubleOrNull() ?: 0.0
                    val sKw = socketsKw.toDoubleOrNull() ?: 0.0
                    val hKw = hvacKw.toDoubleOrNull() ?: 0.0
                    val wKw = waterHeatersKw.toDoubleOrNull() ?: 0.0

                    val groups = listOf(
                        LoadScheduleCalculator.LoadGroup("إنارة", lKw, 0.8),
                        LoadScheduleCalculator.LoadGroup("أفياش", sKw, 0.6),
                        LoadScheduleCalculator.LoadGroup("تكييف", hKw, 0.9),
                        LoadScheduleCalculator.LoadGroup("سخانات", wKw, 0.7)
                    )
                    result = LoadScheduleCalculator.calculate(groups)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حساب إجمالي الأحمال واللوحة", fontWeight = FontWeight.Bold)
            }
        }

        if (result != null) {
            item {
                CalculatorResultCard(
                    title = "ملخص دراسة أحمال اللوحة",
                    metrics = listOf(
                        Pair("إجمالي الأحمال الموصولة", "${result!!.totalConnectedKw} kW (${result!!.totalConnectedKva} kVA)"),
                        Pair("أقصى حمل متزامن (Demand)", "${result!!.totalDemandKw} kW (${result!!.totalDemandKva} kVA)"),
                        Pair("تيار اللوحة المحسوب", "${result!!.estimatedMainCurrentAmps} أمبير"),
                        Pair("القاطع الرئيسي الموصى به", "${result!!.recommendedMainBreakerAmps} أمبير (3P)")
                    ),
                    formula = "I = Demand kW ÷ (1.732 × 380 × 0.85)",
                    steps = result!!.steps,
                    showSteps = true,
                    onToggleSteps = {},
                    onCopy = {
                        copyToClipboard(context, "أقصى حمل: ${result!!.totalDemandKw}kW | تيار: ${result!!.estimatedMainCurrentAmps}A | قاطع: ${result!!.recommendedMainBreakerAmps}A")
                    },
                    onSave = {
                        viewModel.saveCalculationRecord(
                            title = "حساب أحمال مشروع",
                            category = "loads",
                            inputSummary = "إنارة=$lightingKw, تكييف=$hvacKw kW",
                            resultSummary = "طلب=${result!!.totalDemandKw}kW، قاطع=${result!!.recommendedMainBreakerAmps}A"
                        )
                    }
                )
            }
        }

        item { DisclaimerFooter() }
    }
}

// -------------------------------------------------------------
// 8. Unit Converter View
// -------------------------------------------------------------
@Composable
fun UnitConverterView(viewModel: ElectricianViewModel) {
    var kwInput by remember { mutableStateOf("7.5") }
    var hpResult by remember { mutableStateOf("10.06") }

    var hpInput by remember { mutableStateOf("5") }
    var kwResult by remember { mutableStateOf("3.73") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تحويل القدرة: كيلوواط (kW) إلى حصان ميكانيكي (HP)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    UnitNumberField(
                        value = kwInput,
                        onValueChange = {
                            kwInput = it
                            val kw = it.toDoubleOrNull() ?: 0.0
                            hpResult = "${UnitConverter.kwToHp(kw)}"
                        },
                        label = "القدرة بالكيلوواط",
                        unit = "kW"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "النتيجة: $hpResult حصان (HP)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberGoldPrimary)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تحويل القدرة: حصان (HP) إلى كيلوواط (kW)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    UnitNumberField(
                        value = hpInput,
                        onValueChange = {
                            hpInput = it
                            val hp = it.toDoubleOrNull() ?: 0.0
                            kwResult = "${UnitConverter.hpToKw(hp)}"
                        },
                        label = "القدرة بالحصان",
                        unit = "HP"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "النتيجة: $kwResult كيلوواط (kW)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberGoldPrimary)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("جدول التحويل السريع لمقاييس الأسلاك الأمريكية (AWG إلى ملم²):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    UnitConverter.awgTable.forEach { (awg, mm2) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(awg, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("يعادل $mm2 ملم²", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Shared Result Card & Components
// -------------------------------------------------------------
@Composable
fun CalculatorResultCard(
    title: String,
    metrics: List<Pair<String, String>>,
    formula: String,
    steps: List<String>,
    showSteps: Boolean,
    onToggleSteps: () -> Unit,
    onCopy: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "حفظ في السجل", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Grid
            metrics.chunked(2).forEach { rowMetrics ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMetrics.forEach { (label, value) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            if (formula.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المعادلة: $formula",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                steps.forEach { step ->
                    Text("• $step", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DisclaimerFooter() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "النتائج الحسابية للاسترشاد الهندسي، ولا تغني عن مراجعة الكود واللوائح الفنية المحلية المعتمدة في موقع العمل.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Electrician Calculator Result", text)
    clipboard.setPrimaryClip(clip)
}
