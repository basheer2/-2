package com.example

import com.example.data.model.Invoice
import com.example.data.model.InvoiceItem
import com.example.data.model.Project
import com.example.data.model.ProjectLaborItem
import com.example.data.model.ProjectMaterialItem
import com.example.domain.calc.BreakerSizeCalculator
import com.example.domain.calc.CableSizeCalculator
import com.example.domain.calc.EnergyConsumptionCalculator
import com.example.domain.calc.OhmLawCalculator
import com.example.domain.calc.PowerCalculator
import com.example.domain.calc.UnitConverter
import com.example.domain.calc.VoltageDropCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElectricalCalculationsTest {

    @Test
    fun testOhmLawWithVoltageAndCurrent() {
        // V = 220V, I = 10A -> R = 22Ω, P = 2200W
        val result = OhmLawCalculator.calculate(v = 220.0, i = 10.0)
        assertEquals(22.0, result.resistance, 0.01)
        assertEquals(2200.0, result.power, 0.01)
    }

    @Test
    fun testOhmLawWithResistanceAndPower() {
        // R = 10Ω, P = 1000W -> V = √(1000 * 10) = 100V, I = √(1000 / 10) = 10A
        val result = OhmLawCalculator.calculate(r = 10.0, p = 1000.0)
        assertEquals(100.0, result.voltage, 0.01)
        assertEquals(10.0, result.current, 0.01)
    }

    @Test
    fun testSinglePhasePower() {
        // V = 220V, cosφ = 0.85, P = 5.5 kW -> I = (5500) / (220 * 0.85) ≈ 29.41 A
        val result = PowerCalculator.calculate(
            phaseType = PowerCalculator.PhaseType.SINGLE_PHASE,
            voltage = 220.0,
            powerFactor = 0.85,
            activePowerKw = 5.5
        )
        assertEquals(29.41, result.currentAmperes, 0.1)
        assertEquals(6.47, result.apparentPowerKva, 0.1)
    }

    @Test
    fun testThreePhasePower() {
        // 3-Phase 380V, cosφ = 0.85, P = 15 kW -> I = (15000) / (√3 * 380 * 0.85) ≈ 26.82 A
        val result = PowerCalculator.calculate(
            phaseType = PowerCalculator.PhaseType.THREE_PHASE,
            voltage = 380.0,
            powerFactor = 0.85,
            activePowerKw = 15.0
        )
        assertEquals(26.82, result.currentAmperes, 0.2)
        assertEquals(17.65, result.apparentPowerKva, 0.2)
    }

    @Test
    fun testVoltageDropCalculation() {
        // 1-Phase, 220V, I=20A, L=30m, Copper (0.0178), A=4mm²
        // ΔV = (2 * 0.0178 * 30 * 20) / 4 = 5.34 V
        // %ΔV = (5.34 / 220) * 100 = 2.43% (Compliant <= 3%)
        val result = VoltageDropCalculator.calculate(
            isThreePhase = false,
            nominalVoltage = 220.0,
            currentAmps = 20.0,
            lengthMeters = 30.0,
            wireAreaMm2 = 4.0,
            material = VoltageDropCalculator.ConductorMaterial.COPPER
        )
        assertEquals(5.34, result.voltageDropVolts, 0.05)
        assertEquals(2.43, result.voltageDropPercent, 0.05)
        assertTrue(result.isCompliantBranch)
    }

    @Test
    fun testCableSizeSelector() {
        // Current 25A, Conduit in wall (derating 0.8), Ambient temp 35°C
        val result = CableSizeCalculator.calculate(
            loadCurrentAmps = 25.0,
            voltage = 220.0,
            isThreePhase = false,
            lengthMeters = 25.0,
            ambientTempC = 35.0
        )
        // Expected selected cable size >= 4.0 or 6.0 mm²
        assertTrue(result.recommendedSizeMm2 >= 4.0)
        assertTrue(result.breakerRecommendationAmps >= 31.25)
    }

    @Test
    fun testCircuitBreakerSizingContinuousLoad() {
        // Continuous load I = 28A -> 28 * 1.25 = 35A -> Next standard rating = 40A
        val result = BreakerSizeCalculator.calculate(
            loadAmps = 28.0,
            isThreePhase = false,
            loadType = BreakerSizeCalculator.LoadType.CONTINUOUS
        )
        assertEquals(40.0, result.recommendedRatingAmps, 0.01)
        assertEquals(35.0, result.minimumDesignAmps, 0.01)
    }

    @Test
    fun testEnergyConsumptionAndCost() {
        // 2000 W water heater, 5 hours/day, 30 days/month, 0.20 currency/kWh
        // Daily kWh = (2000 * 5) / 1000 = 10 kWh
        // Monthly kWh = 10 * 30 = 300 kWh
        // Monthly cost = 300 * 0.20 = 60.0
        val result = EnergyConsumptionCalculator.calculate(
            powerWatts = 2000.0,
            hoursPerDay = 5.0,
            daysPerMonth = 30.0,
            costPerKwh = 0.20
        )
        assertEquals(10.0, result.dailyKwh, 0.01)
        assertEquals(300.0, result.monthlyKwh, 0.01)
        assertEquals(60.0, result.monthlyCost, 0.01)
    }

    @Test
    fun testUnitConverter() {
        // 10 HP ≈ 7.46 kW
        val kw = UnitConverter.hpToKw(10.0)
        assertEquals(7.46, kw, 0.05)
        // 7.46 kW ≈ 10 HP
        val hp = UnitConverter.kwToHp(7.46)
        assertEquals(10.0, hp, 0.1)
    }

    @Test
    fun testInvoiceFinancialCalculations() {
        // 2 items: 100 x 5 = 500, 200 x 2 = 400. Subtotal = 900.
        // Discount = 100. Taxable = 800.
        // VAT = 15% -> 800 * 0.15 = 120. Grand total = 920.
        // Paid = 500. Remaining due = 420.
        val invoice = Invoice(
            invoiceNumber = "INV-TEST",
            clientName = "عميل تجريبي",
            items = listOf(
                InvoiceItem("سلك 2.5", 5.0, 100.0),
                InvoiceItem("قاطع 32A", 2.0, 200.0)
            ),
            overallDiscount = 100.0,
            isTaxEnabled = true,
            taxRate = 15.0,
            paidAmount = 500.0
        )
        assertEquals(900.0, invoice.subtotal, 0.01)
        assertEquals(800.0, invoice.taxableAmount, 0.01)
        assertEquals(120.0, invoice.taxAmount, 0.01)
        assertEquals(920.0, invoice.grandTotal, 0.01)
        assertEquals(420.0, invoice.remainingDue, 0.01)
    }

    @Test
    fun testProjectFinancialsAndProfit() {
        // Agreed amount = 10,000
        // Materials = 3,000, Labor = 2,500, Expenses = 500
        // Total cost = 6,000 -> Profit = 4,000 -> Margin = 40%
        val project = Project(
            name = "مشروع تجريبي",
            clientName = "عميل تجريبي",
            agreedAmount = 10000.0,
            materials = listOf(ProjectMaterialItem(name = "كابلات", quantity = 10.0, unit = "متر", unitCost = 300.0, unitSellingPrice = 350.0)),
            labor = listOf(ProjectLaborItem(description = "تمديد", workerName = "فني", daysOrHours = 5.0, rate = 500.0)),
            expenses = listOf(com.example.data.model.ProjectExpenseItem(description = "نقل", amount = 500.0))
        )
        assertEquals(3000.0, project.totalMaterialsCost, 0.01)
        assertEquals(2500.0, project.totalLaborCost, 0.01)
        assertEquals(500.0, project.totalExpensesCost, 0.01)
        assertEquals(6000.0, project.totalProjectCost, 0.01)
        assertEquals(4000.0, project.estimatedProfit, 0.01)
        assertEquals(40.0, project.profitMarginPercent, 0.01)
    }
}
