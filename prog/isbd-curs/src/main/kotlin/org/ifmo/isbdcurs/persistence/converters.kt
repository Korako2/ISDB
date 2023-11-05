package org.ifmo.isbdcurs.persistence

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlinx.datetime.*
import java.sql.Timestamp

@Converter(autoApply = true)
class LocalDateConverter : AttributeConverter<LocalDate, Timestamp> {
    override fun convertToDatabaseColumn(attribute: LocalDate?): Timestamp? {
        if (attribute == null) {
            return null
        }
        return Timestamp(attribute.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
    }

    override fun convertToEntityAttribute(dbData: Timestamp?): LocalDate? {
        if (dbData == null) {
            return null
        }
        return dbData.toLocalDateTime().toKotlinLocalDateTime().date;
    }
}

@Converter(autoApply = true)
class InstantConverter : AttributeConverter<Instant, java.time.Instant> {
    override fun convertToDatabaseColumn(attribute: Instant?): java.time.Instant? {
        if (attribute == null) {
            return null
        }
        return attribute.toJavaInstant()
    }

    override fun convertToEntityAttribute(dbData: java.time.Instant?): Instant? {
        if (dbData == null) {
            return null
        }
        return dbData.toKotlinInstant()
    }
}

@Converter(autoApply = true)
class LocalTimeConverter : AttributeConverter<LocalTime, java.sql.Time> {
    override fun convertToDatabaseColumn(attribute: LocalTime?): java.sql.Time? {
        if (attribute == null) {
            return null
        }
        return java.sql.Time.valueOf(attribute.toString())
    }

    override fun convertToEntityAttribute(dbData: java.sql.Time?): LocalTime? {
        if (dbData == null) {
            return null
        }
        return LocalTime.parse(dbData.toString())
    }
}