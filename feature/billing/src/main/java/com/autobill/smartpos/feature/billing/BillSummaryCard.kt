package com.autobill.smartpos.feature.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.BillStatus
import com.autobill.smartpos.feature.billing.R

/**
 * Reusable bill summary card — shows bill number, line items, tax breakdown,
 * and total. Used on both the Billing screen (after generation) and a receipt view.
 */
@Composable
fun BillSummaryCard(
    bill: Bill,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top,
        ) {
            Column {
                Text(
                    text       = bill.billNumber,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF212121),
                )
                bill.restaurantName?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                }
            }
            BillStatusChip(status = bill.status)
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(modifier = Modifier.height(10.dp))

        // ── Line items ──────────────────────────────────────────────────────
        if (bill.billItems.isNotEmpty()) {
            bill.billItems.forEach { item ->
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = item.foodName,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = Color(0xFF212121),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text  = "${item.quantity} × ₹%.2f".format(item.unitPrice),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E),
                        )
                    }
                    Text(
                        text       = "₹%.2f".format(item.itemTotal),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color      = Color(0xFF212121),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── Tax breakdown ───────────────────────────────────────────────────
        BillRow(label = stringResource(R.string.bill_subtotal), value = "₹%.2f".format(bill.subtotal))
        if (bill.cgstAmount > 0) {
            BillRow(
                label = stringResource(R.string.bill_cgst),
                value = "₹%.2f".format(bill.cgstAmount),
                labelColor = Color(0xFF757575),
            )
        }
        if (bill.sgstAmount > 0) {
            BillRow(
                label = stringResource(R.string.bill_sgst),
                value = "₹%.2f".format(bill.sgstAmount),
                labelColor = Color(0xFF757575),
            )
        }
        if (bill.discountAmount > 0) {
            BillRow(
                label      = stringResource(R.string.bill_discount),
                value      = "−₹%.2f".format(bill.discountAmount),
                labelColor = Color(0xFF388E3C),
                valueColor = Color(0xFF388E3C),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.5.dp, color = Color(0xFFBDBDBD))
        Spacer(modifier = Modifier.height(8.dp))

        // ── Total ───────────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text       = stringResource(R.string.bill_total),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF212121),
            )
            Text(
                text       = "₹%.2f".format(bill.totalAmount),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF212121),
            )
        }

        // ── Partial / Remaining ─────────────────────────────────────────────
        if (bill.status == BillStatus.PARTIAL && bill.paidAmount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            BillRow(
                label      = stringResource(R.string.bill_paid_so_far),
                value      = "₹%.2f".format(bill.paidAmount),
                labelColor = Color(0xFF388E3C),
                valueColor = Color(0xFF388E3C),
            )
            BillRow(
                label      = stringResource(R.string.bill_remaining),
                value      = "₹%.2f".format(bill.remainingAmount),
                labelColor = Color(0xFFE33E3E),
                valueColor = Color(0xFFE33E3E),
            )
        }
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    labelColor: Color = Color(0xFF424242),
    valueColor: Color = Color(0xFF212121),
) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = labelColor)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
fun BillStatusChip(status: BillStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        BillStatus.ISSUED    -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        BillStatus.PARTIAL   -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        BillStatus.PAID      -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        BillStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFB00020)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg, modifier = modifier) {
        Text(
            text     = status.value,
            style    = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color    = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
