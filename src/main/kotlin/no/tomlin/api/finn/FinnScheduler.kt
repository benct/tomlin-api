package no.tomlin.api.finn

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FinnScheduler {

    @Autowired
    private lateinit var finnController: FinnController

    @Scheduled(cron = "0 0 3,9,15,21 * * ?")
    fun trackAllPrices() {
        finnController.track()
    }
}
