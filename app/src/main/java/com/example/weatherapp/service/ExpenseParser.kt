package com.example.weatherapp.service

import android.util.Xml
import com.example.weatherapp.model.Expense
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object ExpenseParser {

    fun parseCsv(inputStream: InputStream): List<Expense> {
        val result = mutableListOf<Expense>()
        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val parsed = parseSingleLine(line)
                    if (parsed != null) {
                        result.add(parsed)
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun parseRawCsvText(text: String): List<Expense> {
        val result = mutableListOf<Expense>()
        try {
            val lines = text.split("\n")
            for (line in lines) {
                val parsed = parseSingleLine(line)
                if (parsed != null) {
                    result.add(parsed)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun parseSingleLine(line: String): Expense? {
        val parts = line.split(",")
        if (parts.size >= 5) {
            val date = parts[0].trim().removeSurrounding("\"")
            val name = parts[1].trim().removeSurrounding("\"")
            val category = parts[2].trim().removeSurrounding("\"")
            val amountStr = parts[3].trim().removeSurrounding("\"")
            val type = parts[4].trim().removeSurrounding("\"")
            val amount = amountStr.toDoubleOrNull() ?: return null
            if (category.isNotEmpty() && 
                category.lowercase() != "expense category" && 
                category.lowercase() != "category" && 
                category.lowercase() != "expense" &&
                category.lowercase() != "type"
            ) {
                return Expense(name, category, amount, date, type)
            }
        } else if (parts.size == 4) {
            val date = parts[0].trim().removeSurrounding("\"")
            val name = parts[1].trim().removeSurrounding("\"")
            val category = parts[2].trim().removeSurrounding("\"")
            val amountStr = parts[3].trim().removeSurrounding("\"")
            val amount = amountStr.toDoubleOrNull() ?: return null
            if (category.isNotEmpty() && 
                category.lowercase() != "expense category" && 
                category.lowercase() != "category" && 
                category.lowercase() != "expense"
            ) {
                return Expense(name, category, amount, date)
            }
        } else if (parts.size == 3) {
            val date = parts[0].trim().removeSurrounding("\"")
            val category = parts[1].trim().removeSurrounding("\"")
            val amountStr = parts[2].trim().removeSurrounding("\"")
            val amount = amountStr.toDoubleOrNull() ?: return null
            if (category.isNotEmpty() && 
                category.lowercase() != "expense category" && 
                category.lowercase() != "category" && 
                category.lowercase() != "expense"
            ) {
                return Expense(category, category, amount, date)
            }
        } else if (parts.size == 2) {
            val category = parts[0].trim().removeSurrounding("\"")
            val amountStr = parts[1].trim().removeSurrounding("\"")
            val amount = amountStr.toDoubleOrNull() ?: return null
            if (category.isNotEmpty() && 
                category.lowercase() != "expense category" && 
                category.lowercase() != "category" && 
                category.lowercase() != "expense"
            ) {
                return Expense(category, category, amount, "")
            }
        }
        return null
    }

    fun parseXlsx(inputStream: InputStream): List<Expense> {
        val sharedStrings = mutableListOf<String>()
        var sheetBytes: ByteArray? = null
        var sharedStringsBytes: ByteArray? = null

        try {
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "xl/sharedStrings.xml" -> {
                            sharedStringsBytes = zip.readBytes()
                        }
                        "xl/worksheets/sheet1.xml" -> {
                            sheetBytes = zip.readBytes()
                        }
                    }
                    entry = zip.nextEntry
                }
            }

            if (sharedStringsBytes != null) {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(sharedStringsBytes), "UTF-8")
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "t") {
                        parser.next()
                        if (parser.eventType == XmlPullParser.TEXT) {
                            sharedStrings.add(parser.text)
                        } else if (parser.eventType == XmlPullParser.END_TAG && parser.name == "t") {
                            sharedStrings.add("")
                        }
                    }
                    eventType = parser.next()
                }
            }

            val result = mutableListOf<Expense>()
            if (sheetBytes != null) {
                val parser = Xml.newPullParser()
                parser.setInput(ByteArrayInputStream(sheetBytes), "UTF-8")
                var eventType = parser.eventType
                var currentCellRef = ""
                var currentCellType = ""
                var inValue = false
                var currentValStr = ""

                var lastRow = -1
                var currentA = ""
                var currentB = ""
                var currentC = ""
                var currentD = ""

                val emitRow = {
                    val date4 = currentA.trim()
                    val name4 = currentB.trim()
                    val cat4 = currentC.trim()
                    val amt4 = currentD.toDoubleOrNull() ?: 0.0

                    val cat2 = currentA.trim()
                    val amt2 = currentB.toDoubleOrNull() ?: 0.0

                    if (cat4.isNotEmpty() && amt4 > 0.0 && 
                        cat4.lowercase() != "expense category" && cat4.lowercase() != "category" && cat4.lowercase() != "expense"
                    ) {
                        result.add(Expense(name4, cat4, amt4, date4))
                    } else if (cat2.isNotEmpty() && amt2 > 0.0 && 
                        cat2.lowercase() != "expense category" && cat2.lowercase() != "category" && cat2.lowercase() != "expense"
                    ) {
                        result.add(Expense(cat2, cat2, amt2, ""))
                    }
                }

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (parser.name == "c") {
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                        } else if (parser.name == "v") {
                            inValue = true
                        }
                    } else if (eventType == XmlPullParser.TEXT && inValue) {
                        currentValStr = parser.text
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (parser.name == "v") {
                            inValue = false
                        } else if (parser.name == "c") {
                            val value = if (currentCellType == "s") {
                                val idx = currentValStr.toIntOrNull() ?: -1
                                if (idx in sharedStrings.indices) sharedStrings[idx] else currentValStr
                            } else {
                                currentValStr
                            }

                            val row = currentCellRef.filter { it.isDigit() }.toIntOrNull() ?: -1
                            val col = currentCellRef.filter { it.isLetter() }

                            if (row != lastRow) {
                                if (lastRow != -1) {
                                    emitRow()
                                }
                                currentA = ""
                                currentB = ""
                                currentC = ""
                                currentD = ""
                                lastRow = row
                            }

                            when (col) {
                                "A" -> currentA = value
                                "B" -> currentB = value
                                "C" -> currentC = value
                                "D" -> currentD = value
                            }
                            currentValStr = ""
                        }
                    }
                    eventType = parser.next()
                }
                if (lastRow != -1) {
                    emitRow()
                }
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
}
