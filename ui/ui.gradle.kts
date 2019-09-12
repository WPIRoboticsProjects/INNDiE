plugins {
    id("org.gretty") version "2.3.1"
    id("com.devsoap.vaadin-flow") version "1.2"
}

vaadin {
    version = "14.0.4"
    isProductionMode = false
    isSubmitStatistics = false

    autoconfigure()
}
