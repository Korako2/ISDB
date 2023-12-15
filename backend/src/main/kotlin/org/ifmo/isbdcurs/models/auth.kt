package org.ifmo.isbdcurs.models

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_gen")
    @SequenceGenerator(name = "customer_gen", sequenceName = "customer_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val username: String,
    var password: String,
    var email: String,
    var phone: String,
    var isAdmin: Boolean
)
