package com.mokocharlie.model

import java.sql.Timestamp

case class User(id: Long,
                password: String,
                lastLogin: Timestamp,
                isSuperuser: Boolean,
                email: String,
                firstName: String,
                lastName: String,
                isStaff: Boolean,
                isActive: Boolean,
                dateJoined: Timestamp)
