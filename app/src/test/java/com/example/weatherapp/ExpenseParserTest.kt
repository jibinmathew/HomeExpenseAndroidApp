package com.example.weatherapp

import com.example.weatherapp.service.ExpenseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExpenseParserTest {

    @Test
    fun parseRawCsvText_threeColumns_success() {
        val rawText = """
            2026-05-28, Food, 15.75
            2026-05-29, Transport, 35.00
        """.trimIndent()

        val parsed = ExpenseParser.parseRawCsvText(rawText)
        
        assertEquals(2, parsed.size)
        
        assertEquals("Food", parsed[0].category)
        assertEquals(15.75, parsed[0].amount, 0.001)
        assertEquals("2026-05-28", parsed[0].date)

        assertEquals("Transport", parsed[1].category)
        assertEquals(35.00, parsed[1].amount, 0.001)
        assertEquals("2026-05-29", parsed[1].date)
    }

    @Test
    fun parseRawCsvText_twoColumns_success() {
        val rawText = """
            Shopping, 120.50
            Utilities, 85.00
        """.trimIndent()

        val parsed = ExpenseParser.parseRawCsvText(rawText)
        
        assertEquals(2, parsed.size)
        
        assertEquals("Shopping", parsed[0].category)
        assertEquals(120.50, parsed[0].amount, 0.001)
        assertEquals("", parsed[0].date)

        assertEquals("Utilities", parsed[1].category)
        assertEquals(85.00, parsed[1].amount, 0.001)
        assertEquals("", parsed[1].date)
    }

    @Test
    fun parseRawCsvText_withHeaders_ignored() {
        val rawText = """
            Date, Category, Amount
            2026-05-28, Food, 12.00
            Category, Amount
            Rent, 1200.00
        """.trimIndent()

        val parsed = ExpenseParser.parseRawCsvText(rawText)
        
        assertEquals(2, parsed.size)
        
        assertEquals("Food", parsed[0].category)
        assertEquals(12.00, parsed[0].amount, 0.001)
        assertEquals("2026-05-28", parsed[0].date)

        assertEquals("Rent", parsed[1].category)
        assertEquals(1200.00, parsed[1].amount, 0.001)
        assertEquals("", parsed[1].date)
    }

    @Test
    fun parseRawCsvText_userSpecificData_success() {
        val rawText = """
            Date,Expense Description,Category,Amount
            05/26/2026,Visa Debit purchase - 4631 UBER CANADA U E,Transportation,15.13
            05/23/2026,Insurance - PEACE HILLS GEN,Insurance,382.76
            05/22/2026,Contactless Interac purchase - 1225 Alforno Bakery,Dining / Food,6.40
            05/19/2026,Online Banking payment - 7455 TELUS COMMUN CA,Utilities / Telecom,95.00
            05/19/2026,Online Banking payment - 3651 KOODO MOBILE,Utilities / Telecom,124.32
            05/19/2026,e-Transfer sent u p n Scotia AV79LK,Transfer,200.00
            05/19/2026,Online Banking transfer - 3244,Transfer,950.88
            05/15/2026,Monthly fee,Bank Fees,12.95
            05/14/2026,e-Transfer sent u p n Scotia KHRUQ5,Transfer,98.00
            05/14/2026,Online Banking transfer - 5124,Transfer,120.00
            05/13/2026,Visa Debit purchase - 6795 LYFT *TEMP AU,Transportation,8.97
            05/12/2026,Contactless Interac purchase - CBTS SS CALGARY AB,Transit,12.40
            05/12/2026,Contactless Interac purchase - 2585 SPARKLE SOLUTIO,Retail / Services,30.00
            05/12/2026,Online Banking transfer - 7815,Transfer,71.00
            05/12/2026,Online Banking transfer - 5085,Transfer,324.24
            05/11/2026,Visa Debit purchase - 4623 LYFT *TEMP AU,Transportation,5.10
            05/06/2026,Visa Debit purchase - 6205 READING ROOM,Dining / Entertainment,65.00
            05/04/2026,Mrtg Payment/kupling -kercen,Housing / Mortgage,1744.00
            05/01/2026,Visa Debit purchase - 7855 WESTLAND EXPRES,Retail / Services,17.39
        """.trimIndent()

        val parsed = ExpenseParser.parseRawCsvText(rawText)
        
        assertEquals(19, parsed.size)
        
        assertEquals("Transportation", parsed[0].category)
        assertEquals(15.13, parsed[0].amount, 0.001)
        assertEquals("05/26/2026", parsed[0].date)

        assertEquals("Housing / Mortgage", parsed[17].category)
        assertEquals(1744.00, parsed[17].amount, 0.001)
        assertEquals("05/04/2026", parsed[17].date)
    }
}
