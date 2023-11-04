package org.ifmo.isbdcurs.persistence

import kotlinx.datetime.*
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.sql.Date
import java.sql.Timestamp

@WritingConverter
class LocalDateToTimestampConverter : Converter<LocalDate, Timestamp> {
    override fun convert(source: LocalDate): Timestamp {
        return Timestamp(source.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
    }
}

@ReadingConverter
class TimestampToLocalDateConverter : Converter<Timestamp, LocalDate> {
    override fun convert(source: Timestamp): LocalDate {
        return source.toLocalDateTime().toKotlinLocalDateTime().date;
    }
}

@ReadingConverter
class DateToLocalDateConverter : Converter<Date, LocalDate> {
    override fun convert(source: java.sql.Date): LocalDate {
        return source.toLocalDate().toKotlinLocalDate()
    }
}

@WritingConverter
class InstantToTimestampConverter : Converter<Instant, Timestamp> {
    override fun convert(source: Instant): Timestamp {
        return Timestamp(source.toEpochMilliseconds())
    }
}

@ReadingConverter
class TimestampToInstantConverter : Converter<Timestamp, Instant> {
    override fun convert(source: Timestamp): Instant {
        return source.toInstant().toKotlinInstant()
    }
}