pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "in-my-element"

// "In my element" — tablero del Honda Element 2003-2006.
//   :element  la app: su perfil, su motor, su tablero, sus tablas de averias
//   :comun    la libreria: Bluetooth, OBD, baterias, llantas, tablero
include(":comun")
include(":element")
