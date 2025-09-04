package aftsundayServer.aftsunday_minis3.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")

class AppProperties{
    lateinit var storageRoot: String
    lateinit var apiKey: String
    lateinit var presignSecret: String
    var presignDefaultTtlSeconds: Long = 600
}