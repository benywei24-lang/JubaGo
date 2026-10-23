package com.example

import com.example.data.JubaGoRepository
import com.example.model.BusinessRegistrationRequest
import com.example.model.CoverageScope
import com.example.model.ServiceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VendorRegistrationTest {

    @Test
    fun testBusinessRegistrationRequestCreation() {
        val request = BusinessRegistrationRequest(
            id = "vnd_test_001",
            businessName = "Wau Nile Star Hotel",
            businessType = "Hotel & Hospitality",
            category = ServiceCategory.HOTELS,
            coverageScope = CoverageScope.SOUTH_SUDAN_NATIONAL,
            cityAndCountry = "Wau, South Sudan",
            physicalAddress = "Main Market Road, Plot 5",
            contactPhone = "+211 92 111 2233",
            contactEmail = "wau@nilestar.com",
            registrationTaxNumber = "SSD-TIN-WAU-01",
            serviceOfferingTitle = "Deluxe Nile Suite",
            serviceDescription = "Executive air-conditioned suites with solar backup",
            priceSsp = 65000,
            priceUsd = 50.0,
            priceUnit = "/night",
            isVerified = true,
            registeredAt = "2026-10-15 14:00"
        )

        assertEquals("vnd_test_001", request.id)
        assertEquals("Wau Nile Star Hotel", request.businessName)
        assertEquals(ServiceCategory.HOTELS, request.category)
        assertEquals(CoverageScope.SOUTH_SUDAN_NATIONAL, request.coverageScope)
        assertEquals("Wau, South Sudan", request.cityAndCountry)
        assertEquals(65000L, request.priceSsp)
        assertEquals(50.0, request.priceUsd, 0.001)
        assertTrue(request.isVerified)
    }

    @Test
    fun testRegisterBusinessInRepository() {
        val repo = JubaGoRepository()
        val request = BusinessRegistrationRequest(
            id = "vnd_test_002",
            businessName = "Malakal Fresh Fish Co.",
            businessType = "Restaurant & Dining",
            category = ServiceCategory.FOOD,
            coverageScope = CoverageScope.SOUTH_SUDAN_NATIONAL,
            cityAndCountry = "Malakal, South Sudan",
            physicalAddress = "Riverbank Port Area",
            contactPhone = "+211 91 333 4455",
            contactEmail = "malakal@fish.com",
            registrationTaxNumber = "SSD-TIN-MAL-02",
            serviceOfferingTitle = "Fresh Nile Tilapia Platter",
            serviceDescription = "Daily catch prepared with traditional seasoning",
            priceSsp = 12000,
            priceUsd = 10.0,
            priceUnit = "/meal",
            isVerified = true,
            registeredAt = "2026-10-15 14:00"
        )

        val serviceItem = repo.registerBusiness(request)

        assertNotNull(serviceItem)
        assertEquals("vnd_test_002", serviceItem.id)
        assertEquals("Fresh Nile Tilapia Platter", serviceItem.title)
        assertEquals("Malakal Fresh Fish Co.", serviceItem.providerName)
        assertEquals(ServiceCategory.FOOD, serviceItem.category)
        assertEquals(CoverageScope.SOUTH_SUDAN_NATIONAL, serviceItem.coverageScope)
        assertTrue(serviceItem.isVerified)

        val found = repo.services.value.any { it.id == "vnd_test_002" }
        assertTrue("Newly registered business must be present in repository services", found)
    }

    @Test
    fun testGlobalCoverageScopeRegistration() {
        val repo = JubaGoRepository()
        val request = BusinessRegistrationRequest(
            id = "vnd_test_003",
            businessName = "Dubai-Juba Express Air Freight",
            businessType = "Air Travel & Cargo",
            category = ServiceCategory.FLIGHTS,
            coverageScope = CoverageScope.GLOBAL_BEYOND_AFRICA,
            cityAndCountry = "Dubai, United Arab Emirates",
            physicalAddress = "Cargo Village, Terminal 2",
            contactPhone = "+971 4 200 0000",
            contactEmail = "cargo@dubai-juba.ae",
            registrationTaxNumber = "DXB-CARGO-99",
            serviceOfferingTitle = "Direct Air Freight DXB to JUB",
            serviceDescription = "Express commercial cargo shipping direct to Juba airport",
            priceSsp = 350000,
            priceUsd = 285.0,
            priceUnit = "/shipment",
            isVerified = true,
            registeredAt = "2026-10-15 14:00"
        )

        val service = repo.registerBusiness(request)
        assertEquals(CoverageScope.GLOBAL_BEYOND_AFRICA, service.coverageScope)
        assertTrue(service.tags.contains("Dubai, United Arab Emirates"))
    }
}
