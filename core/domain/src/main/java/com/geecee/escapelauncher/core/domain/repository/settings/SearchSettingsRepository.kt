package com.geecee.escapelauncher.core.domain.repository.settings

import com.geecee.escapelauncher.core.model.SearchGestureDirection
import kotlinx.coroutines.flow.Flow

interface SearchSettingsRepository {
    val showSearchBox: Flow<Boolean>
    suspend fun setShowSearchBox(enabled: Boolean)
    val searchAutoOpen: Flow<Boolean>
    suspend fun setSearchAutoOpen(enabled: Boolean)
    val automaticallyOpenAppsInSearch: Flow<Boolean>
    suspend fun setAutomaticallyOpenAppsInSearch(enabled: Boolean)
    val showHiddenAppsInSearch: Flow<Boolean>
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)
    val searchGestureDirection: Flow<SearchGestureDirection>
    suspend fun setSearchGestureDirection(direction: SearchGestureDirection)
}
