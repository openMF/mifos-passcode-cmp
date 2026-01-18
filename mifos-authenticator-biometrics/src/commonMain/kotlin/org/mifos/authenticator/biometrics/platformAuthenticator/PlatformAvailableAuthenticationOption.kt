/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE.md
 */
package org.mifos.authenticator.biometrics.platformAuthenticator

import kotlinx.coroutines.flow.StateFlow

expect class PlatformAvailableAuthenticationOption private constructor() {

    constructor(context: Any? = null)
    val currentAuthOption: StateFlow<List<PlatformAuthOptions>>
}
