pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // The SDK itself (com.flipp:dvm-sdk).
        // Credentials are read from ~/.gradle/gradle.properties so they are never committed:
        //   artifactory_user=[PROVIDED BY FLIPP]
        //   artifactory_password=[PROVIDED BY FLIPP]
        maven {
            url = uri("https://flipplib.jfrog.io/artifactory/dvm-sdk-android")
            credentials {
                username = providers.gradleProperty("artifactory_user").orNull ?: ""
                password = providers.gradleProperty("artifactory_password").orNull ?: ""
            }
        }
    }
}

rootProject.name = "DVM Sample"
include(":app")
