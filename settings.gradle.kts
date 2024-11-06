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
        maven {
            url = uri("https://flipplib.jfrog.io/artifactory/dvm-sdk-android")
            credentials {
                username = ""        // TODO: Artifactory username provided by Flipp
                password = ""        // TODO: Artifactory password provided by Flipp
            }
        }
    }
}

rootProject.name = "DVM Sample"
include(":app")
 