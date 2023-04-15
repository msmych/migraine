package uk.matvey.persistence

import org.jooq.Converter
import java.sql.Timestamp
import java.time.Instant

class InstantConverter : Converter<Timestamp, Instant> {

    override fun from(timestamp: Timestamp?): Instant? {
        return timestamp?.toInstant()
    }

    override fun to(instant: Instant?): Timestamp? {
        return instant?.let(Timestamp::from)
    }

    override fun fromType(): Class<Timestamp> {
        return Timestamp::class.java
    }

    override fun toType(): Class<Instant> {
        return Instant::class.java
    }
}
