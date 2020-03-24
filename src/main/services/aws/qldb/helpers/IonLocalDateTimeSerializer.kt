package main.services.aws.qldb.helpers

import com.amazon.ion.Timestamp
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.dataformat.ion.IonGenerator
import framework.models.Handler
import java.io.IOException
import java.time.LocalDateTime

/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */



/**
 * Serializes [java.time.LocalDate] to Ion.
 */
class IonLocalDateTimeSerializer :
    StdScalarSerializer<LocalDateTime>(LocalDateTime::class.java) {
    @Throws(IOException::class)
    override fun serialize(
        date: LocalDateTime,
        jsonGenerator: JsonGenerator,
        serializerProvider: SerializerProvider
    ) {
        try {
            val timestamp =
                Timestamp.forSecond(
                    date.year,
                    date.monthValue,
                    date.dayOfMonth,
                    date.hour,
                    date.minute,
                    date.second,
                    0
                )
            (jsonGenerator as IonGenerator).writeValue(timestamp)
        } catch(e: Exception) {
            println("FOUND ERROR IN serialize: ${e.message}")
            println("Timestamp is: ${Handler.gson.toJson(date)}")
            e.printStackTrace()
            throw e
        }
    }
}