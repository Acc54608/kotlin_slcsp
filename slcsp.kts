#!/usr/bin/env kscript
@file:DependsOn("com.github.doyaaaaaken:kotlin-csv-jvm:1.1.0")
@file:DependsOn("com.github.doyaaaaaken:kotlin-csv-js:1.1.0")
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File

// Initialize csv reader an writer file objects and the 2 maps I used
val slc = File("slcsp.csv")
val plans = File("plans.csv")
val zip = File("zips.csv")
val reader = csvReader()
val writer = csvWriter()
val zipmap = mutableMapOf<String, String>()
val ratePlans = mutableMapOf<String, MutableList<Double>>()

//Get zips I need to solve sclsp for in map
reader.open(slc){
    readAllAsSequence().forEach { row: List<String> ->
        if (row[0] == "zipcode")
            return@forEach
        zipmap[row[0]] = row[1]
    }
}

// This foreach reader is for finding each zipcode needed for the solution in zipcodes.csv and giving each zipcode key in zipMap the rateArea as the value
reader.open(zip){
    readAllAsSequence().forEach { row: List<String> ->
        val rateArea = Pair(row[1], row[4]).toString()
        if (zipmap.containsKey(row[0])){
            if (zipmap[row[0]].isNullOrEmpty()) {
                zipmap[row[0]] = rateArea
                ratePlans[rateArea] = mutableListOf()
            } 
            // If the program finds another rateArea for a certain zipcode then that zipocode is ambiguous and the slcsp can't be found so its value will be empty
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
        // This if will simply find each silver plan for a matching rate area ans add it to a list so we can compare them later
        if (ratePlans.containsKey(rateArea)) {
            if (row[2] == "Silver")
                ratePlans[rateArea]?.add(row[3].toDouble())
        }
    }
}

ratePlans.forEach { entry ->
    entry.value.sort()
    // Sort the entry's value to get them in order to easily find the 2nd lowest then I filter entries because 2 zipcodes can have the same rate area
    val zipMatches = zipmap.entries.filter { it.value == entry.key }
    zipMatches.forEach { zip ->
        // Another loop for the filtered entries incase of doubles then assign each zipcode key in zipmap the 2nd lowest cost plan 
        if (!entry.value.isNullOrEmpty() && entry.value.size > 2)
            zipmap[zip.key] = String.format("%.2f", entry.value[1])
        else 
        // In case of the zip having more rate areas from the 2nd loop the answer is empty or if it has 1 or 0 silver plans it doesn't have a 2nd lowest.
            zipmap[zip.key] = ""
    }
}

// This just writes the answer into a new file then then I open that file and read it to sdtout
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
