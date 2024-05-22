package com.example.trekly.composeable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trekly.model.Activity
import com.example.trekly.util.formatActivityDuration
import com.example.trekly.util.formatActivityTimes

@Composable
fun ActivityCard(
    activity: Activity,
    fromUTCTimeMillis: Long,
    toUTCTimeMillis: Long,
    photo: Bitmap?,
    updateActivity: (Activity) -> Unit,
    deleteActivity: (Int) -> Unit,
    modifier: Modifier,
) {
    var isDialogVisible by remember { mutableStateOf(false) }

    if (isDialogVisible) {
        ActivityDialog(
            activity = activity,
            onDismissRequest = { isDialogVisible = false },
            onConfirmation = { isDialogVisible = false },
            mutateActivity = updateActivity,
            deleteActivity = deleteActivity,
            fromUTCTimeMillis = fromUTCTimeMillis,
            toUTCTimeMillis = toUTCTimeMillis,
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        onClick = { isDialogVisible = true },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(2f)
            ) {
                if (activity.place != null) {
                    Text(
                        text = activity.place.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${
                        formatActivityTimes(
                            activity.fromTime,
                            activity.toTime
                        )
                    } (${formatActivityDuration(activity.fromTime, activity.toTime)})"
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (activity.notes != "") {
                    Text(text = activity.notes)
                }
            }
            if (photo != null) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = "Photo of ${activity.place?.name ?: "activity"}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .width(100.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}