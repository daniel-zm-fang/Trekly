package com.example.trekly.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.trekly.viewmodel.ItineraryMapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryMapView(
    itineraryMapViewModel: ItineraryMapViewModel,
    itineraryId: Int,
    navigateBack: () -> Unit
) {
    val sheetState = rememberBottomSheetScaffoldState(
        SheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false,
            density = LocalDensity.current,
            skipHiddenState = true
        )
    )

    LaunchedEffect(Unit) {
        itineraryMapViewModel.initializeData(itineraryId)
    }

    BottomSheetScaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Itinerary",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to home page"
                        )
                    }
                },
                actions = {
//                    IconButton(onClick = { /* do something */ }) {
//                        Icon(
//                            imageVector = Icons.Filled.Share,
//                            contentDescription = "Share this itinerary"
//                        )
//                    }
                },
            )
        },
        sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        scaffoldState = sheetState,
        sheetContent = {
            ItineraryView(
                viewModel = itineraryMapViewModel,
                itineraryId = itineraryId,
            )
        },
        content = {
            MapsView(viewModel = itineraryMapViewModel, itineraryId = itineraryId)
        },
    )
}