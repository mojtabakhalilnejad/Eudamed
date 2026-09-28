package com.openregulatory.eudamedsearch.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openregulatory.eudamedsearch.data.model.Device
import com.openregulatory.eudamedsearch.data.model.DeviceCertificateInfo
import com.openregulatory.eudamedsearch.data.model.DeviceDetailResponse
import com.openregulatory.eudamedsearch.data.repository.DeviceRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    device: Device,
    onBack: () -> Unit,
    repository: DeviceRepository = DeviceRepository()
) {
    var detail by remember { mutableStateOf<DeviceDetailResponse?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(device.id) {
        loading = true
        detail = repository.getDeviceDetail(device)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.tradeName.ifBlank { "جزئیات دستگاه" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                InfoRow("نام تجاری", device.tradeName)
                InfoRow("کمپانی سازنده", device.manufacturerName)
                InfoRow("کشور سازنده", device.manufacturerCountry.orEmpty())
                InfoRow("SRN کمپانی", device.manufacturerSrn.orEmpty())
                InfoRow("کلاس ریسک", device.riskClassLabel.orEmpty())
                InfoRow("وضعیت دستگاه", device.deviceStatusLabel.orEmpty())
                InfoRow("نوع قانون", device.legislationLabel.orEmpty())
                InfoRow("Primary DI", device.primaryDi.orEmpty())
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            if (loading) {
                item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            }

            detail?.let { d ->
                item {
                    Text(
                        "گواهی‌های CE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                val certs = d.deviceCertificateInfoList.orEmpty()
                if (certs.isEmpty()) {
                    item { Text("گواهی CE ثبت‌شده‌ای برای این نسخه یافت نشد.") }
                } else {
                    items(certs) { cert -> CertificateRow(cert) }
                }
            }

            if (!loading && detail == null) {
                item {
                    Text(
                        "جزئیات کامل فقط برای دستگاه‌هایی که از سایت EUDAMED آمده‌اند قابل نمایش است، یا سرور پاسخی برنگرداند.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CertificateRow(cert: DeviceCertificateInfo) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            cert.certificateNumber ?: "شماره گواهی نامشخص",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        cert.certificateType?.humanize()?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        cert.notifiedBody?.name?.let { Text("مرجع ناظر: $it", style = MaterialTheme.typography.bodyMedium) }
        cert.issueDate?.let { Text("تاریخ صدور: $it", style = MaterialTheme.typography.bodyMedium) }
        cert.certificateExpiry?.let { Text("تاریخ انقضا: $it", style = MaterialTheme.typography.bodyMedium) }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
