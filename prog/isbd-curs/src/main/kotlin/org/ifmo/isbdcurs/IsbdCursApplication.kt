package org.ifmo.isbdcurs

import org.ifmo.isbdcurs.logic.FillTables
import org.ifmo.isbdcurs.persistence.DateToLocalDateConverter
import org.ifmo.isbdcurs.persistence.LocalDateToTimestampConverter
import org.ifmo.isbdcurs.persistence.TimestampToLocalDateConverter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@SpringBootApplication
class IsbdCursApplication

fun main(args: Array<String>) {
    val applicationContext = runApplication<IsbdCursApplication>(*args)

    val fillTables = applicationContext.getBean(FillTables::class.java)
    fillTables.fill();
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

