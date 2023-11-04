package org.ifmo.isbdcurs.persistence

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.ifmo.isbdcurs.models.BodyType
import org.ifmo.isbdcurs.models.Person
import org.ifmo.isbdcurs.models.Vehicle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class RepositoryTests {

    @Autowired
    lateinit var repository: PersonRepository

    @Autowired
    lateinit var vehicleRepository: VehicleRepository

    @BeforeEach
    fun before() {
        repository.deleteAll()
    }

    @Test
    fun `should insert vehicle`() {
        val vehicle = Vehicle(
            plateNumber = "П123УП78",
            model = "Lada",
            manufactureYear = Instant.parse("2020-01-01T00:00:00Z"),
            length = 1.0,
            width = 1.0,
            height = 1.0,
            loadCapacity = 1.0,
            bodyType = BodyType.CLOSED,
        )
        vehicleRepository.save(vehicle);
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
