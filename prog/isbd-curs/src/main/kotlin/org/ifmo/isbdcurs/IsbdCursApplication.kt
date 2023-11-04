package org.ifmo.isbdcurs

import kotlinx.datetime.*
import org.ifmo.isbdcurs.persistence.DateToLocalDateConverter
import org.ifmo.isbdcurs.persistence.LocalDateToTimestampConverter
import org.ifmo.isbdcurs.persistence.TimestampToLocalDateConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import java.sql.Timestamp

@SpringBootApplication
class ApplicationConfiguration

fun main(args: Array<String>) {
    runApplication<ApplicationConfiguration>(*args)
}
@Configuration
class DbConfiguration : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions {
        return JdbcCustomConversions(
            mutableListOf(
                LocalDateToTimestampConverter(),
                TimestampToLocalDateConverter(),
                DateToLocalDateConverter(),
            )
        )
    }
}

