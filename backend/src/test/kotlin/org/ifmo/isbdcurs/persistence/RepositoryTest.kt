package org.ifmo.isbdcurs.persistence

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import org.assertj.core.api.Assertions.assertThat
import org.ifmo.isbdcurs.models.*
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

    @Autowired
    lateinit var movementHistoryRepository: VehicleMovementHistoryRepository

    @BeforeEach
    fun before() {
        repository.deleteAll()
        movementHistoryRepository.deleteAll()
        vehicleRepository.deleteAll()
    }

    val vehicle = Vehicle(
        plateNumber = "П123УП78",
        model = "Lada",
        manufactureYear = Instant.parse("2020-01-01T00:00:00Z").toJavaInstant(),
        length = 1.0,
        width = 1.0,
        height = 1.0,
        loadCapacity = 1.0,
        bodyType = BodyType.CLOSED,
    )

    @Test
    fun `should insert vehicle`() {
        vehicleRepository.save(vehicle);
        assertThat(vehicleRepository.findAll().first().bodyType).isEqualTo(vehicle.bodyType)
    }

    @Test
    fun `should insert composite key entity`() {
        vehicleRepository.save(vehicle);
        val movementHistory = VehicleMovementHistory(
            vehicleId = vehicle.id!!,
            date = Instant.parse("2020-01-01T00:00:00Z").toJavaInstant(),
            latitude = 1.0f,
            longitude = 1.0f,
            mileage = 1.0f,
        )
        movementHistoryRepository.save(movementHistory)
        assertThat(movementHistoryRepository.findAll().first().latitude).isEqualTo(movementHistory.latitude)
        assertThat(movementHistoryRepository.findAll().first().mileage).isEqualTo(movementHistory.mileage)
    }
}
