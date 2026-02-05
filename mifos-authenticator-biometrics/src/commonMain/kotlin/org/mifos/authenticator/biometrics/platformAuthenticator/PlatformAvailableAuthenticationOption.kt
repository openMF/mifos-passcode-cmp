/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.biometrics.platformAuthenticator

import kotlinx.coroutines.flow.StateFlow

/**
 * A class that provides information about the available platform authentication options on a device.
 *
 * This class is used to query the device for the types of authentication methods that are
 * supported and available to the user.
 *
 * @property currentAuthOption A [StateFlow] that emits a list of [PlatformAuthOptions]
 * currently available on the device.
 */
expect class PlatformAvailableAuthenticationOption private constructor() {

    /**
     * Initializes the PlatformAvailableAuthenticationOption instance.
     *
     * @param context A platform-specific context object. On Android, this should be an
     * instance of `Context`. On other platforms, it can be `null`.
     */
    constructor(context: Any? = null)

    /**
     * A [StateFlow] that emits a list of [PlatformAuthOptions] currently available on the device.
     */
    val currentAuthOption: StateFlow<List<PlatformAuthOptions>>
}
