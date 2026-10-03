package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RepairJob
import com.example.localization.AppStrings
import com.example.util.UpiHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairsScreen(
    jobs: List<RepairJob>,
    allJobs: List<RepairJob>,
    searchQuery: String,
    statusFilter: String,
    strings: AppStrings,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onNewJobClick: () -> Unit,
    onJobClick: (RepairJob) -> Unit,
    onOpenQr: (RepairJob) -> Unit,
    onSendWhatsApp: (RepairJob) -> Unit,
    onMarkReady: (RepairJob) -> Unit,
    onMarkDelivered: (RepairJob) -> Unit,
    onDeleteJob: (RepairJob) -> Unit
) {
    val context = LocalContext.current

    val pendingCount = allJobs.count { it.status == RepairJob.STATUS_PENDING || it.status == RepairJob.STATUS_IN_PROGRESS }
    val readyCount = allJobs.count { it.status == RepairJob.STATUS_READY_FOR_PICKUP }
    val totalCollected = allJobs.filter { it.isPaid }.sumOf { it.estimatedAmount }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("repairs_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = strings.pendingRepairs,
                        value = pendingCount.toString(),
                        bgColor = Color(0xFFFEF3C7),
                        textColor = Color(0xFF92400E),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = strings.readyPickup,
                        value = readyCount.toString(),
                        bgColor = Color(0xFFD1FAE5),
                        textColor = Color(0xFF065F46),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = strings.totalRevenue,
                        value = UpiHelper.formatInr(totalCollected),
                        bgColor = Color(0xFFE0E7FF),
                        textColor = Color(0xFF3730A3),
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text(strings.searchJobs) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("repairs_search_input")
                )
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to strings.allJobs,
                        RepairJob.STATUS_PENDING to strings.statusPending,
                        RepairJob.STATUS_IN_PROGRESS to strings.statusInProgress,
                        RepairJob.STATUS_READY_FOR_PICKUP to strings.statusReady,
                        RepairJob.STATUS_DELIVERED to strings.statusDelivered
                    ).forEach { (code, label) ->
                        FilterChip(
                            selected = statusFilter == code,
                            onClick = { onStatusFilterChange(code) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Empty state
            if (jobs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No repair tickets found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Jobs List
            items(jobs, key = { it.id }) { job ->
                RepairJobCard(
                    job = job,
                    strings = strings,
                    onOpenQr = { onOpenQr(job) },
                    onSendWhatsApp = { onSendWhatsApp(job) },
                    onMarkReady = { onMarkReady(job) },
                    onMarkDelivered = { onMarkDelivered(job) },
                    onEdit = { onJobClick(job) },
                    onDelete = { onDeleteJob(job) }
                )
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onNewJobClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("new_repair_job_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.newJob)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = strings.newJob, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun RepairJobCard(
    job: RepairJob,
    strings: AppStrings,
    onOpenQr: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onMarkReady: () -> Unit,
    onMarkDelivered: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    val (statusLabel, statusBg, statusText) = when (job.status) {
        RepairJob.STATUS_READY_FOR_PICKUP -> Triple(strings.statusReady, Color(0xFFD1FAE5), Color(0xFF065F46))
        RepairJob.STATUS_IN_PROGRESS -> Triple(strings.statusInProgress, Color(0xFFDBEAFE), Color(0xFF1E40AF))
        RepairJob.STATUS_DELIVERED -> Triple(strings.statusDelivered, Color(0xFFF1F5F9), Color(0xFF475569))
        else -> Triple(strings.statusPending, Color(0xFFFEF3C7), Color(0xFF92400E))
    }

    val (payLabel, payBg, payText) = when (job.paymentStatus) {
        RepairJob.PAYMENT_PAID_UPI -> Triple(strings.paidUpi, Color(0xFFD1FAE5), Color(0xFF047857))
        RepairJob.PAYMENT_PAID_CASH -> Triple(strings.paidCash, Color(0xFFE0E7FF), Color(0xFF3730A3))
        else -> Triple(strings.unpaid, Color(0xFFFEE2E2), Color(0xFF991B1B))
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("repair_card_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Token #, Device Model, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = job.tokenNumber,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${job.deviceBrand} ${job.deviceModel}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusText
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Info & Phone Call Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = job.customerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = job.customerPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Issue description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🔧 ${job.issueDescription}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bill & Payment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.estimatedAmount,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = UpiHelper.formatInr(job.estimatedAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(payBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = payLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = payText
                        )
                    }
                    if (job.paymentUpiRef.isNotBlank()) {
                        Text(
                            text = "Ref: ${job.paymentUpiRef}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dynamic UPI QR Pay
                FilledTonalButton(
                    onClick = onOpenQr,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pay_upi_qr_job_${job.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "UPI QR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                // Send WhatsApp Notification to Customer
                Button(
                    onClick = onSendWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("whatsapp_job_${job.id}"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "WhatsApp", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                // Quick Status Advance
                if (job.status != RepairJob.STATUS_READY_FOR_PICKUP && job.status != RepairJob.STATUS_DELIVERED) {
                    OutlinedButton(
                        onClick = onMarkReady,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ready", style = MaterialTheme.typography.labelMedium)
                    }
                } else if (job.status == RepairJob.STATUS_READY_FOR_PICKUP) {
                    OutlinedButton(
                        onClick = onMarkDelivered,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Delivered", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
