#!/usr/bin/env kscript
@file:DependsOn("com.github.doyaaaaaken:kotlin-csv-jvm:1.1.0")
@file:DependsOn("com.github.doyaaaaaken:kotlin-csv-js:1.1.0")
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

val slc = File("slcsp.csv")
val plans = File("plans.csv")
val zip = File("zips.csv")
val reader = csvReader()
val writer = csvWriter()
val zipmap = mutableMapOf<String, String>()
val ratePlans = mutableMapOf<String, MutableList<Double>>()

reader.open(slc){
    readAllAsSequence().forEach { row: List<String> ->
        if (row[0] == "zipcode")
            return@forEach
        zipmap[row[0]] = row[1]
    }
}

reader.open(zip){
    readAllAsSequence().forEach { row: List<String> ->
        val rateArea = Pair(row[1], row[4]).toString()
        if (zipmap.containsKey(row[0])){
            if (zipmap[row[0]].isNullOrEmpty()) {
                zipmap[row[0]] = rateArea
                ratePlans[rateArea] = mutableListOf()
            }
            else if (zipmap[row[0]] != rateArea) {
                zipmap[row[0]] = ""
                ratePlans.remove(rateArea)
            }
        }
    }
}

reader.open(plans) {
    readAllAsSequence().forEach { row: List<String> ->
        val rateArea = Pair(row[1], row[4]).toString()
        if (ratePlans.containsKey(rateArea)) {
            if (row[2] == "Silver")
                ratePlans[rateArea]?.add(row[3].toDouble())
        }
    }
}

ratePlans.forEach { entry ->
    entry.value.sort()
    val zipMatches = zipmap.entries.filter { it.value == entry.key }
    zipMatches.forEach { zip ->
        if (!entry.value.isNullOrEmpty() && entry.value.size > 2)
            zipmap[zip.key] = String.format("%.2f", entry.value[1])
        else
            zipmap[zip.key] = ""
    }
}

writer.open("slcspChanged.csv") {
    var row = listOf("zipcode", "rate")
    writeRow(row)
    zipmap.forEach() { entry ->
        row = listOf(entry.key, entry.value)
        writeRow(row)
    }
}

reader.open("slcspChanged.csv"){
    println("Output:")
    readAllAsSequence().forEach { list ->
        println(list)
    }
}