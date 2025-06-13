package com.mifos.passcode.context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal

/**
 * Represents an abstract context for the application that provides platform-specific
 * functionality. This class must be implemented in each platform-specific source set.
 */
expect abstract class AppContext

/**
 * A composition local that provides the current [AppContext] to the composition tree.
 * This allows composable functions to access the platform-specific context without
 * explicit parameters.
 */
expect val LocalContext: ProvidableCompositionLocal<AppContext>

/**
 * The platform-specific activity or view controller associated with the current context.
 *
 * This property is accessible only from within a Composable function.
 * The return type is [Any] to support different platform-specific types
 * (Activity on Android, UIViewController on iOS, etc.).
 */
@get:Composable
expect val AppContext.activity: Any
