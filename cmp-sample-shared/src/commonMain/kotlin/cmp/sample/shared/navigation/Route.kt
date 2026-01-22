/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package cmp.sample.shared.navigation

import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object PasscodeScreen : Route()

    @Serializable
    data object DeviceAuthScreen : Route()

    @Serializable
    data object LoginScreen : Route()

    @Serializable
    data object HomeScreen : Route()

    @Serializable
    data object ChooseAuthOptionScreen : Route()
}
