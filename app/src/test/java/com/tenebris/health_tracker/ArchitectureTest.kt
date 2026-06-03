package com.tenebris.health_tracker

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureTest {

    private val allModulesScope
        get() = Konsist.scopeFromProject() +
            Konsist.scopeFromModule(":core:model") +
            Konsist.scopeFromModule(":core:data") +
            Konsist.scopeFromModule(":core:ui") +
            Konsist.scopeFromModule(":feature:dashboard") +
            Konsist.scopeFromModule(":feature:onboarding") +
            Konsist.scopeFromModule(":feature:tracking") +
            Konsist.scopeFromModule(":feature:settings")

    @Test
    fun `layer dependency rules are respected`() {
        allModulesScope.assertArchitecture {
            val dataModel = Layer("Data Model", "com.tenebris.health_tracker.data.model..")
            val dataLayer = Layer("Data Layer", "com.tenebris.health_tracker.data..")
            val uiLayer = Layer("UI Layer", "com.tenebris.health_tracker.ui..")
            val coreDi = Layer("Core DI", "com.tenebris.health_tracker.core..")
            val featureDi = Layer("Feature DI", "com.tenebris.health_tracker.feature..")

            dataModel.dependsOnNothing()
            dataLayer.dependsOn(dataModel)
            uiLayer.dependsOn(dataModel)
            coreDi.dependsOn(dataLayer)
            featureDi.dependsOn(dataLayer, uiLayer, coreDi)
        }
    }

    @Test
    fun `core model has zero Android imports`() {
        Konsist
            .scopeFromModule(":core:model")
            .files
            .assertTrue {
                it.imports.none { import ->
                    import.name.startsWith("android.") || import.name.startsWith("androidx.")
                }
            }
    }

    @Test
    fun `feature modules do not import other feature modules`() {
        listOf(
            ":feature:dashboard",
            ":feature:onboarding",
            ":feature:tracking",
            ":feature:settings",
        ).forEach { module ->
            Konsist.scopeFromModule(module).files.assertTrue { file ->
                val filePackage = file.packagee?.name ?: return@assertTrue true
                file.imports.none { import ->
                    import.name.startsWith("com.tenebris.health_tracker.feature") &&
                        import.name != filePackage
                }
            }
        }
    }
}
