package com.bachebazi.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bachebazi.app.ui.AppViewModel
import com.bachebazi.app.ui.BacheBaziTheme
import com.bachebazi.app.ui.BackgroundLayer
import com.bachebazi.app.ui.IntroOverlay
import com.bachebazi.app.ui.MenuScreen
import com.bachebazi.app.ui.Pal
import com.bachebazi.app.ui.RandomScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent { BacheBaziTheme { BacheBaziApp() } }
    }
}

private const val ANIM = 300

@Composable
fun BacheBaziApp(vm: AppViewModel = viewModel()) {
    // Persian UI: always right-to-left.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var introDone by rememberSaveable { mutableStateOf(false) }
        val nav = rememberNavController()
        val snack = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            vm.messages.collect { msg ->
                launch {
                    snack.currentSnackbarData?.dismiss()
                    snack.showSnackbar(msg)
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            BackgroundLayer(vm)

            NavHost(
                navController = nav,
                startDestination = "menu",
                enterTransition = { slideIntoContainer(SlideDirection.Start, tween(ANIM)) + fadeIn(tween(ANIM)) },
                exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(ANIM)) + fadeOut(tween(200)) },
                popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(ANIM)) + fadeIn(tween(ANIM)) },
                popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(ANIM)) + fadeOut(tween(200)) },
            ) {
                composable("menu") {
                    MenuScreen(vm = vm, onOpenRandom = { nav.navigate("random") { launchSingleTop = true } })
                }
                composable("random") {
                    RandomScreen(vm = vm, onBack = { nav.popBackStack() })
                }
            }

            SnackbarHost(
                hostState = snack,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Pal.Surface2,
                    contentColor = Pal.Text,
                )
            }

            if (!introDone) IntroOverlay(onFinished = { introDone = true })
        }
    }
}
