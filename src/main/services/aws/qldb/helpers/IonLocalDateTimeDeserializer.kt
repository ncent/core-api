package main.services.aws.qldb.helpers

import com.amazon.ion.Timestamp
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException
import java.time.LocalDate
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
 * Deserializes [java.time.LocalDateTime] from Ion.
 */
class IonLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    @Throws(IOException::class)
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext
    ): LocalDateTime {
        return timestampToLocalDateTime(jp.embeddedObject as Timestamp)
    }

    private fun timestampToLocalDateTime(timestamp: Timestamp): LocalDateTime {
        try {
            return LocalDateTime.of(
                timestamp.year,
                timestamp.month,
                timestamp.day,
                timestamp.hour,
                timestamp.minute,
                timestamp.second
            )
        } catch(e: Exception) {
            println("FOUND ERROR IN timestampToLocalDateTime: ${e.message}")
            println("Timestamp is: $timestamp")
            e.printStackTrace()
            throw e
        }
    }
}