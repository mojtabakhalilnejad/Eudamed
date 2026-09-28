package com.openregulatory.eudamedsearch.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openregulatory.eudamedsearch.data.model.Device

@Composable
fun DeviceListItem(device: Device, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device.tradeName.ifBlank { "(بدون نام تجاری)" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = device.manufacturerName.ifBlank { "کمپانی نامشخص" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val subtitleParts = listOfNotNull(
                device.manufacturerCountry,
                device.primaryDi?.let { "UDI: $it" }
            )
            if (subtitleParts.isNotEmpty()) {
                Text(
                    text = subtitleParts.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                device.riskClassLabel?.takeIf { it.isNotBlank() }?.let {
                    AssistChip(onClick = onClick, label = { Text(it) })
                }
                device.deviceStatusLabel?.takeIf { it.isNotBlank() }?.let {
                    AssistChip(onClick = onClick, label = { Text(it) })
                }
                device.legislationLabel?.takeIf { it.isNotBlank() }?.let {
                    AssistChip(onClick = onClick, label = { Text(it) })
                }
            }
        }
    }
}
