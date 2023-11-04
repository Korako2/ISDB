package org.ifmo.isbdcurs.persistence

import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.ifmo.isbdcurs.models.Person
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class RepositoryTests {

    @Autowired
    lateinit var repository: PersonRepository

    @BeforeEach
    fun before() {
        repository.deleteAll()
    }

    @Test
    fun `should find one person`() {

        repository.save(
            Person(
                firstName = "Walter",
                lastName = "White",
                middleName = null,
                gender = 'M',
                dateOfBirth = LocalDate(1980, 1, 1),
            )
        )

        val walter = repository.findAll().first()

        assertThat(walter).isNotNull()
        assertThat(walter.firstName).isEqualTo("Walter")
        assertThat(walter.lastName).isEqualTo("White")
    }
}
